package testpackage.com.services

import testpackage.com.entities.{NodeType, PathwaysGrid}
import testpackage.com.traits.Locator

import scala.collection.immutable._
import scala.collection.mutable
import scala.concurrent.{Await, Promise}
import scala.util.Try

// place a default thread pool in scope on which the Future will be executed
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class LocateWithParallel extends Locator {
	val msgLevel: Int = 4

	// Find the shorted path from the starting waypoint node to a deposit location
	// Return the best path as determined by the shorted travel time
	def findPathToDepositLocation(startNodeId: String): List[String] = {
		bestTimeToDepLoc = PathwaysGrid.maxTravelTime
		bestPathToDepLoc = List()
		LogService.log(s"findPathToDepositLocation: StartNodeId: $startNodeId", "INFO", 2)

		// get the Node of the Starting Location
		val waypointNode = PathwaysGrid.nodeGridMap(startNodeId)
		// Using the startNodeId - traverse the list of connections for this node (edges)
		// for each edge from the start node - dispatch a new thread to traverse the connections
		for ((nextNodeId: String, distance: Int) <- waypointNode) {
			// start the solution path and track the time distance
			val solPath: List[String] = List(nextNodeId, startNodeId)
			// pass each of the next edge nodes on to the searchWayPointEdges
			// wait for results from search path future
			// the first element in the list will be the total time
			// the last element in the list will be the starting node
			searchWayPointEdges(nextNodeId, distance, solPath).onComplete {
				case Success(result) =>
					if (result.isCompleted) {
						val resultList = result.value.head.get
						if (resultList.nonEmpty) {
							val time: Int = resultList.head.toInt
							val path: List[String] = resultList.tail
							LogService.log(s"Result: $result  time: $time  path: $path", "PATH", msgLevel)
							// Check if valid path is shorter and store total time and path
							if (time < bestTimeToDepLoc) {
								// update best time and store result
								bestTimeToDepLoc = time
								bestPathToDepLoc = bestTimeToDepLoc.toString :: path
							}
						} else {
							LogService.log(s"Result: $result  list: $resultList  EMPTY", "PATH", msgLevel)
						}
					}
				case Failure(e) => LogService.log(s"Error: $e", "ERRR", 7)
			}
			Thread.sleep(100)
		}
		bestPathToDepLoc.reverse
	}


	def searchWayPointEdges(nodeId: String, distance: Int, curPath: List[String]): Future[Future[List[String]]] = Future {
		var newPathTime: List[String] = List()
		// if the node type is a deposit location, then this is a complete path
		if ({PathwaysGrid.nodeMap(nodeId)} == NodeType.DEP) {
			// this is a complete path to a deposit location
			val solPath = nodeId :: curPath
			newPathTime = distance.toString :: solPath
		} else {
			// start the solution path and track the time distance
			val nextNode = PathwaysGrid.nodeGridMap(nodeId)
			newPathTime = searchWayPointNode(nextNode, curPath, distance)
		}
		LogService.log(s"searchEdges: NodeId: $nodeId  newPathTime: $newPathTime", "PATH", msgLevel)
		Future(newPathTime)
	}


	// Using the startNodeId - traverse the list of connections for this node (edges)
	private def searchWayPointNode(waypointNode: mutable.Map[String, Int], curPath: List[String], totalTime: Int): List[String] = {
		for ((nextNodeId: String, distance: Int) <- waypointNode) {
			// if the node type is a deposit location, then this is a complete path
			if ({PathwaysGrid.nodeMap(nextNodeId)} == NodeType.DEP) {
				// this is a complete path to a deposit location
				val solPath = nextNodeId :: curPath
				val newTime = totalTime + distance
				// TODO: if the time distance is the same - should we use fewer number of waypoints
				// Check if valid path is shorter and store total time and path
				// TODO: is this thread safe - probably need to move this outside of this thread
				//if (newTime < bestTimeToDepLoc) {
				//	// update best time and store result
				//	bestTimeToDepLoc = newTime
				//	bestPathToDepLoc = bestTimeToDepLoc.toString :: solPath
				//}
				val newPathTime = newTime.toString :: solPath
				LogService.log(s"Found: NodeId: $nextNodeId  Path: $solPath  Time: $newTime", "PATH", msgLevel)
				return newPathTime
			} else {
				// if the node type is a way point, continue looking with this new node
				val waypointNode2 = PathwaysGrid.nodeGridMap(nextNodeId)
				// check if exceeded best time or if about to backtrack
				val okTime = totalTime + distance < bestTimeToDepLoc
				val okPath = !curPath.contains(nextNodeId)
				LogService.log(s"Checking: NodeId: $nextNodeId  CurPath: $curPath  ok: $okTime/$okPath", "PATH", msgLevel)
				if (okTime && okPath) {
					searchWayPointNode(waypointNode2, nextNodeId :: curPath, totalTime + distance)
				} else {
					LogService.log(s"Stopping: NodeId: $nextNodeId  CurPath: $curPath", "PATH", msgLevel)
					// Does this need to return something to indicate this path is no longer being followed ???
				}
			}
		}
		List[String]()
	}
}
