package testpackage.com.entities

import testpackage.com.entities.NodeType.NodeType

// Determines if the node type is a waypoint or deposit location
// Deposit locations must match a preset list of named locations
// Waypoints must start with W followed by a number
object Node {
	val depositLocations = Set("SB", "7E", "PTS", "CVS", "FD")

	def determineNodeType(nodeId: String): NodeType = {
		var nodeType: NodeType = NodeType.ERR
		if (depositLocations.contains(nodeId)) { return NodeType.DEP }
		if (nodeId.startsWith("W")) {
			// W followed by number
			nodeType = NodeType.WAY
			try {
				val pattern = "(W)([0-9]+)".r
				val pattern(w, num) = nodeId
				val _: Int = num.toInt
			}
			catch { case e: Exception => nodeType = NodeType.ERR }
		}
		nodeType
	}
}

// Enumeration of valid node types - WAY = waypoint - DEP = deposit location - ERR = invalid node type
object NodeType extends Enumeration {
	type NodeType = Value
	val WAY, DEP, ERR = Value
}
