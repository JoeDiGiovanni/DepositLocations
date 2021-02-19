package testpackage.com.services

import testpackage.com.entities.{NodeType, PathwaysGrid}
import testpackage.com.traits.Locator

import scala.collection.immutable._
import scala.collection.mutable

class LocateWithRecursion extends Locator {

	// Find the shorted path from the starting waypoint node to a deposit location
	// Return the best path as determined by the shorted travel time
	def findPathToDepositLocation(startNodeId: String): List[String] = {
		bestTimeToDepLoc = PathwaysGrid.maxTravelTime
		bestPathToDepLoc = List()
		LogService.log(s"findPathToDepositLocation: StartNodeId: $startNodeId", "INFO", 2)

		// get the Node of the Starting Location
		val waypointNode = PathwaysGrid.nodeGridMap(startNodeId)
		// start the solution path and track the time distance
		val solPath: List[String] = List(startNodeId)
		searchWayPointNode(waypointNode, solPath, 0)
		bestPathToDepLoc.reverse
	}

	// Using the startNodeId - traverse the list of connections for this node (edges)
	private def searchWayPointNode(waypointNode: mutable.Map[String, Int], curPath: List[String], totalTime: Int): Unit = {
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
				LogService.log(s"Found: NodeId: $nextNodeId  Path: $solPath  Time: $newTime OK: ${newTime < bestTimeToDepLoc}", "PATH", 4)
			} else {
				// if the node type is a way point, continue looking with this new node
				val waypointNode2 = PathwaysGrid.nodeGridMap(nextNodeId)
				// check if exceeded best time or if about to backtrack
				val okTime = totalTime + distance < bestTimeToDepLoc
				val okPath = !curPath.contains(nextNodeId)
				LogService.log(s"Trying:  NodeId: $nextNodeId  CurPath: $curPath  ok: $okTime/$okPath", "PATH", 4)
				if (okTime && okPath) {
					searchWayPointNode(waypointNode2, nextNodeId :: curPath, totalTime + distance)
				}
			}
		}
	}
}
