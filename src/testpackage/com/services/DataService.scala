package testpackage.com.services

import testpackage.com.entities.{InputTravelTime, InputUserLocation, InvalidDataException, Node, NodeType, ParsedInputData}

import java.io.{FileNotFoundException, IOException}
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.io.{BufferedSource, Source}

object DataService {
	val defaultFileName: String = "TestData.txt"
	var timeDistanceList:ListBuffer [InputTravelTime] = ListBuffer [InputTravelTime]()
	var userLocationList:ListBuffer [InputUserLocation] = ListBuffer [InputUserLocation]()

	// Read the input data using the command line args to get the input file name
	// Should the command line parameter by omitted, a default file name is used - TestData.txt
	// If the input file cannot be loaded, hard-coded data is used to complete the test
	def readInputData(args:Array[String] ): List[String] = {
		var inputData: List[String] = null
		var inputFileName: String = DataService.defaultFileName

		// check that an input file name was provided on the command line - if not use the default file name
		if (args.length < 1) {
			LogService.log(s"Missing input file name - using default: $inputFileName", "ERRR", 6)
		} else {
			inputFileName = args(0)
		}

		// load the input data from the input file provided
		inputData = loadInputDataFromFile(inputFileName)
		if (inputData == null) {
			// file read error - try again with default file name
			inputData = loadInputDataFromFile(defaultFileName)
			if (inputData == null) {
				// still unable to read data from file - last resort - use default data
				inputData = loadDefaultTestData()
			}
		}

		inputData
	}

	// load input data from input file provided returning a list of strings one per line
	def loadInputDataFromFile(inputFileName: String): List[String] = {
		var inputFile: BufferedSource = null
		var inputData: List[String] = null
		LogService.log(s"Input file name: $inputFileName", "INFO")

		// Read the file contents into a list
		try {
			inputFile = Source.fromFile(inputFileName)
			inputData = inputFile.getLines().toList
		} catch {
			case _: FileNotFoundException => LogService.log(s"File Not Found: $inputFileName", "ERRR", 6)
			case _: IOException => LogService.log(s"File Read Error: $inputFileName", "ERRR", 7)
		} finally {
			if (inputFile != null) inputFile.close
		}

		inputData
	}

	// process a single line of input data adding to userLocationList or timeDistanceList
	def parseInputData(inputData: List[String]): ParsedInputData = {
		// Input Data is comprised of list of tuples (locations + time) and pairs (user + location)
		for (line <- inputData) {
			try {
				processInputDataLine(line)
			} catch {
					case e: InvalidDataException =>
						LogService.log(s"[${e.getMessage}] $line ==> INPUT IGNORED", "ERRR", 6)
			}
		}
		// return the parsed data in two lists:
		// the waypoint / deposit location nodes and the user starting points
		ParsedInputData(timeDistanceList, userLocationList)
	}

	// process a single line of input data adding objects to userLocationList or timeDistanceList
	// Input Data is comprised of list of tuples and pairs
	// Tuples are a pair of location nodes and the time distance between them
	//   ex. W1 W2 4
	//   ex. W4 SB 3
	// Pairs are username and current location (Waypoint) node name
	//   ex. User123 W4
	// throws an InvalidDataException if any errors are encountered with the data inputs
	def processInputDataLine(line: String): (ListBuffer [InputTravelTime], ListBuffer [InputUserLocation]) = {
		try {
			if (line.isEmpty) throw InvalidDataException("No Data Found - SKIPPING")
			val pieces = line.split(" ")

			pieces.length match {
				case 2 =>
					// Two Values: User Name | Starting Way Point Node
					if (IsNumber(pieces(0))) throw InvalidDataException("Invalid Data Format")
					if (Node.determineNodeType(pieces(1)) != NodeType.WAY) throw InvalidDataException(s"Invalid Node: ${pieces(1)}")
					userLocationList += InputUserLocation(pieces(0), pieces(1))
				case 3 =>
					// Three Values: Way Point Node | another Way Point Node or Deposit Location | Time Distance
					if (Node.determineNodeType(pieces(0)) == NodeType.ERR) throw InvalidDataException(s"Invalid Node: ${pieces(0)}")
					if (Node.determineNodeType(pieces(1)) == NodeType.ERR) throw InvalidDataException(s"Invalid Node: ${pieces(1)}")
					timeDistanceList += InputTravelTime(pieces(0), pieces(1), pieces(2).toInt)
				// Some kind of data format error
				case _ => throw InvalidDataException("Invalid Data Format")
			}
			LogService.log(s"[${pieces.length}] $line", "DATA", 2)
		} catch {
			case e: InvalidDataException => throw e
			case _: NumberFormatException => throw InvalidDataException("NumberFormatException")
			case e: Exception => throw InvalidDataException(e.getClass.toString)
		}
		(timeDistanceList, userLocationList)
	}

	// check if the provided string (nodeId) is actually a numeric value
	def IsNumber(string: String): Boolean = {
		// assume the string is an integer or float unless a conversion exception error says otherwise
		var isInteger: Boolean = true
		var isFloat: Boolean = true
		try { val _: Int = string.toInt	} catch { case _: Exception => isInteger = false }
		try {	val _: Float = string.toFloat	} catch {	case _: Exception => isFloat = false }
		isInteger || isFloat
	}

	// load up test data in the event a file is not provided on the command line
	def loadDefaultTestData(): List [String] = {
		val inputData: ListBuffer [String] = ListBuffer()
		inputData.append("W1 W3 4")
		inputData.append("W1 SB 2")
		inputData.append("W2 W4 5")
		inputData.append("W3 W2 3")
		inputData.append("W3 W5 5")
		inputData.append("W3 7E 6")
		inputData.append("W4 W8 4")
		inputData.append("W5 PTS 3")
		inputData.append("W6 W3 3")
		inputData.append("W6 7E 2")
		inputData.append("W7 W5 5")
		inputData.append("W7 7E 2")
		inputData.append("W7 FD 1")
		inputData.append("W8 W9 8")
		inputData.append("W8 CVS 2")
		inputData.append("W9 W10 6")
		inputData.append("W10 CVS 5")
		inputData.append("User123 W9")
		inputData.append("User234 W3")
		inputData.toList
	}
}
