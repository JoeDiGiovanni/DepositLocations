package testpackage.com.entities

import testpackage.com.entities.NodeType.NodeType
import testpackage.com.services.LogService

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

// Singleton object to store the Grid of connections between waypoints and deposit locations
object PathwaysGrid {
	var maxTravelTime: Int = 0
	// Create a nested mutable Map structure to represent a two-dimensional array of connections between points
	var nodeGridMap: mutable.Map[String, mutable.Map[String, Int]] = mutable.Map[String, mutable.Map[String, Int]]()
	// Store a list of all nodes - both waypoints and deposit locations with their node types
	var nodeMap: mutable.Map[String, NodeType] = mutable.Map[String, NodeType]()

	// Build the Grid of the waypoints and deposit locations
	// Allows additional data points to be appended by passing appendNodes=true
	def buildPathwaysGrid(inputDataTimeDistance: ListBuffer[InputTravelTime], appendNodes: Boolean = false): Unit = {
		LogService.log(s"buildPathwaysGrid:", "INFO", 2)

		// Clear any existing data if this is not an append
		if (!appendNodes) {
			nodeGridMap = mutable.Map[String, mutable.Map[String, Int]]()
			nodeMap = mutable.Map[String, NodeType]()
		}

		// Input data is list of tuples: two location nodes and the time distance between them
		for (tupleData <- inputDataTimeDistance) {
			// Use this to set best path travel time
			maxTravelTime += tupleData.travelTime
			// Save the node types in the nodeMap
			saveNodeTypes(tupleData)

			if (tupleData.travelTime > 0) {
				// add or update the pathways grid - adding a new path or updating a travel time
				if (nodeMap(tupleData.nodeIdA) == NodeType.WAY) {
					upsertPathway(tupleData.nodeIdA, tupleData.nodeIdB, tupleData.travelTime)
				} else {
					LogService.log(s"Found Deposit Location - expected Way Point: $tupleData", "WARN", 6)
				}
				// if NodeIdB is NOT a deposit location, then add the reverse edge path
				if (nodeMap(tupleData.nodeIdB) == NodeType.WAY) {
					upsertPathway(tupleData.nodeIdB, tupleData.nodeIdA, tupleData.travelTime)
				}
			} else {
				LogService.log(s"SKIPPING Travel Time of 0: $tupleData", "WARN", 6)
			}
		}
		// for debugging
		PathwaysGrid.showPathwayGrid()
	}

	// Publicly exposed Validation Methods for Nodes - Waypoints and Deposit Locations
	def isValidWayPoint(nodeId: String): Boolean = { nodeMap.contains(nodeId) && nodeMap(nodeId) == NodeType.WAY }
	def isValidDepLoc(nodeId: String): Boolean = { nodeMap.contains(nodeId) && nodeMap(nodeId) == NodeType.DEP }

	// Display (to logs) the Nodes of the two-dimensional grid representing waypoint pathways or edges
	private def showPathwayGrid(): Unit = {
		val nodeIdList = nodeMap.toSeq.sortBy(_._1)
		LogService.log(s"showPathwayGrid: $nodeIdList", "INFO", 2)
		for ((nodeId, nodeType) <- nodeIdList) {
			if (nodeType == NodeType.WAY) {
				if (!(nodeId == "W10")) // re-order
					showGridRow(nodeId)
			}
		}
		showGridRow("W10") // show last
	}

	// Log a single row of the showPathwayGrid output
	private def showGridRow(nodeId: String): Unit = {
		// Use the nodeId to get the entry from the Map
		val node: mutable.Map[String, Int] = nodeGridMap(nodeId)
		val showRow = new StringBuilder(s"NodeID: $nodeId:  ")
		for ((k, v) <- node) {
			showRow.append(s"$k:($v)  ")
		}
		LogService.log(s"showPathwayGrid: $showRow", "GRID")
	}

	// Store a list of all the Nodes and their types - waypoints or deposit locations
	private def saveNodeTypes(tupleData: InputTravelTime): Unit = {
		if (!nodeMap.contains(tupleData.nodeIdA)) {
			nodeMap(tupleData.nodeIdA) = Node.determineNodeType(tupleData.nodeIdA)
		}
		if (!nodeMap.contains(tupleData.nodeIdB)) {
			nodeMap(tupleData.nodeIdB) = Node.determineNodeType(tupleData.nodeIdB)
		}
	}

	// Add (or Update) a pathway/edge between two nodes with the travel distance time
	private def upsertPathway(nodeIdA: String, nodeIdB: String, travelTime: Int): Unit = {
		// Check if the first Node (Way/Dep) already exists
		if (!nodeGridMap.contains(nodeIdA)) {
			nodeGridMap += (nodeIdA -> mutable.Map[String, Int]())
		}

		// If the second node is new, add it to the node map
		if (!nodeGridMap(nodeIdA).contains(nodeIdB)) {
			nodeGridMap(nodeIdA) += (nodeIdB -> travelTime)
			LogService.log(s"Append Node: $nodeIdA  linked to: $nodeIdB  Time: $travelTime", msgCategory = "TIME")
		} else {
			nodeGridMap(nodeIdA)(nodeIdB) = travelTime
			LogService.log(s"Update Node: $nodeIdA  Linked to: $nodeIdB  Time: $travelTime", msgCategory = "TIME")
		}
	}
}
