package testpackage.com

import testpackage.com.entities.{InvalidDataException, ParsedInputData, PathwaysGrid}
import testpackage.com.services.{DataService, LocateWithParallel, LocateWithRecursion, LogService}

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

		// Two ways to solve for the Deposit Location - use Parallel Threads vs Straight Recursion
		//val locator = new LocateWithParallel()
		val locator = new LocateWithRecursion()

		// generate solution paths for each user and start point
		for (user <- parsedInputData.users) {
			try {
				// InvalidDataException is thrown if any errors are found with the user data
				locator.validateUserStartNode(user.userName, user.nodeId)
				val solutionPath = locator.findPathToDepositLocation(user.nodeId)
				// Format user name, waypoints, deposit location and distance into formatted string for output
				val formattedOutput = (user.userName :: solutionPath).mkString(" ")
				println(formattedOutput)
				LogService.log(s"Solution Output: $formattedOutput", "XOUT", 5)
			} catch {
				case e: InvalidDataException => LogService.log(s"${e.message}", "ERRR", 6)
			}
		}

		//LoggerService.showLogs(msgLevel = 7) // msgLevel = 1 shows all log messages; > 5 shows only errors
	}
}
