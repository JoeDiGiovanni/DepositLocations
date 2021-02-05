package testpackage.com

import testpackage.com.entities.{InvalidDataException, ParsedInputData, PathwaysGrid}
import testpackage.com.services.{DataService, LocatorService, LoggerService}

import scala.collection.immutable.List

object Main {

	// Console application used to execute and test the Deposit Locations application
	// Command line takes the name of the text file with the input data to load
	def main(args: Array[String]): Unit = {

		// load and parse the input data - Errors found are reported and bad data is ignored
		val inputData: List[String] = DataService.readInputData(args)
		val parsedInputData: ParsedInputData = DataService.parseInputData(inputData)

		// build the connection paths as a two-dimensional grid
		PathwaysGrid.buildPathwaysGrid(parsedInputData.timeDistanceData)

		// generate solution paths for each user and start point
		for (user <- parsedInputData.users) {
			try {
				// InvalidDataException is thrown if any errors are found with the user data
				LocatorService.validateUserStartNode(user.userName, user.nodeId)
				val solutionPath = LocatorService.findPathToDepositLocation(user.nodeId)
				// Format user name, waypoints, deposit location and distance into formatted string for output
				val formattedOutput = (user.userName :: solutionPath).mkString(" ")
				println(formattedOutput)
				LoggerService.log(s"Solution Output: $formattedOutput", "XOUT", 5)
			} catch {
				case e: InvalidDataException => LoggerService.log(s"${e.message}", "ERRR", 6)
			}
		}

		//LoggerService.showLogs(msgLevel = 7) // msgLevel = 1 shows all log messages; > 5 shows only errors
	}
}
