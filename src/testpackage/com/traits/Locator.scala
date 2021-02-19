package testpackage.com.traits

import testpackage.com.entities.{InvalidDataException, PathwaysGrid}

import scala.collection.immutable.List

trait Locator {
	var bestTimeToDepLoc:Int = 0
	var bestPathToDepLoc:List[String] = _

	// Customize implementation
	def findPathToDepositLocation(startNodeId: String): List[String]

	// Validate the user name and the starting node - throw InvalidDataException if any issues found
	def validateUserStartNode(userName: String, startNodeId: String): Unit = {
		var errorMsg: String = ""
		// Check if the user and start node are valid - if not then throw exception
		if (PathwaysGrid.isValidWayPoint(userName)) errorMsg = "User Name should not be a Way Point"
		if (!PathwaysGrid.isValidWayPoint(startNodeId)) errorMsg = "Starting Way Point is NOT VALID"
		if (PathwaysGrid.isValidDepLoc(startNodeId)) errorMsg = "Starting Point is a Deposit Location"
		if (errorMsg.nonEmpty) throw InvalidDataException(s"$errorMsg: $userName $startNodeId")
	}
}
