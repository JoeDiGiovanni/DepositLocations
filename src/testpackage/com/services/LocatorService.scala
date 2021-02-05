package testpackage.com.services

import testpackage.com.entities.{InvalidDataException, NodeType, PathwaysGrid}

import scala.collection.immutable._
import scala.collection.mutable

object LocatorService {
	var bestTimeToDepLoc:Int = 0
	var bestPathToDepLoc:List[String] = _

	// Validate the user name and the starting node - throw InvalidDataException if any issues found
	def validateUserStartNode(userName: String, startNodeId: String): Unit = {
		var errorMsg: String = ""
		// Check if the user and start node are valid - if not then throw exception
		if (PathwaysGrid.isValidWayPoint(userName)) errorMsg = "User Name should not be a Way Point"
		if (!PathwaysGrid.isValidWayPoint(startNodeId)) errorMsg = "Starting Way Point is NOT VALID"
		if (PathwaysGrid.isValidDepLoc(startNodeId)) errorMsg = "Starting Point is a Deposit Location"
		if (!(errorMsg == "")) {
			throw InvalidDataException(s"$errorMsg: $userName $startNodeId")
		}
	}

	// Find the shorted path from the starting waypoint node to a deposit location
	// Return the best path as determined by the shorted travel time
	def findPathToDepositLocation(startNodeId: String): List[String] = {
		bestTimeToDepLoc = PathwaysGrid.maxTravelTime
		bestPathToDepLoc = List()
		LoggerService.log(s"findPathToDepositLocation: StartNodeId: $startNodeId", "INFO", 2)

		// get the Node of the Starting Location
		val waypointNode = PathwaysGrid.nodeGridMap(startNodeId)
		// start the solution path and track the time distance
		val solPath: List[String] = List(startNodeId)
		searchWayPointNode(waypointNode, solPath, 0)
		bestPathToDepLoc.reverse
	}

	// Using the startNodeId - traverse the list of connections for this node (edges)
	private def searchWayPointNode(waypointNode: mutable.Map[String, Int], curPath: List[String], totalTime: Int) {
		for ((nextNodeId: String, distance: Int) <- waypointNode) {
			// if the node type is a deposit location, then this is a complete path
			if ({PathwaysGrid.nodeMap(nextNodeId)} == NodeType.DEP) {
				// this is a complete path to a deposit location
				val solPath = nextNodeId :: curPath
				val newTime = totalTime + distance
				// TODO: if the time distance is the same - should we use fewer number of waypoints
				// Check if valid path is shorter and store total time and path
				if (newTime < bestTimeToDepLoc)	{
					// update best time and store result
					bestTimeToDepLoc = newTime
					bestPathToDepLoc = bestTimeToDepLoc.toString :: solPath
				}
				LoggerService.log(s"Found: NodeId: $nextNodeId  Path: $solPath  Time: $newTime OK: ${newTime < bestTimeToDepLoc}", "PATH", 4)
			} else {
				// if the node type is a way point, continue looking with this new node
				val waypointNode2 = PathwaysGrid.nodeGridMap(nextNodeId)
				// check if exceeded best time or if about to backtrack
				val okTime = totalTime + distance < bestTimeToDepLoc
				val okPath = !curPath.contains(nextNodeId)
				LoggerService.log(s"Trying:  NodeId: $nextNodeId  CurPath: $curPath  ok: $okTime/$okPath", "PATH", 4)
				if (okTime && okPath) {
					searchWayPointNode(waypointNode2, nextNodeId :: curPath, totalTime + distance)
				}
			}
		}
	}
}
