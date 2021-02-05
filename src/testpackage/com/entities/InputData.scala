package testpackage.com.entities

import scala.collection.mutable.ListBuffer

// Three components of input data - two connected nodes and the travel time between them
case class InputTravelTime(nodeIdA: String, nodeIdB: String, travelTime: Int) {
	override def toString: String = s"$nodeIdA $nodeIdB $travelTime"
	def showData: String = s"$nodeIdA->$nodeIdB=travelTime"
}

// Two components of input data - User Name and the User starting point
case class InputUserLocation(userName: String, nodeId: String) {
}

// Customize Exception class to return Data input and integrity errors
case class InvalidDataException(message: String) extends Exception(message, null) {}

case class ParsedInputData(timeDistanceData: ListBuffer[InputTravelTime], users: ListBuffer[InputUserLocation] ) {
}