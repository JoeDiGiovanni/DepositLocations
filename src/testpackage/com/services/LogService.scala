package testpackage.com.services

import scala.collection.mutable.ListBuffer

// Logger Service class with Log Level support and default config and message levels
// Log Message Levels:  5 = Highest Msg Level  0 = Lowest Level  3 = Default  > 5 for Errors
object LogService {
	private val logErrorThreshold: Int = 5
	private val printRealTime: Boolean = false
	private val logList: ListBuffer[LogMsg] = new ListBuffer[LogMsg]
	private var logCategories = Map("MAIN" -> true, "INIT" -> false, "ERRR" -> true)

	// add message to the list of logged messages
	def log(msg: String, msgCategory: String = "MAIN", msgLevel: Int = 3): Unit = {
		val logMsg: LogMsg = new LogMsg(msg, msgCategory, msgLevel)
		logList.append(logMsg)
		// add any new categories not already in the logCategories list
		if (!logCategories.contains(msgCategory)) logCategories += (msgCategory -> true)
		// real-time output for debugging
		if (printRealTime || msgLevel > logErrorThreshold) println(s"Log: $logMsg")
	}

	// display logged messages to the console -
	// if msgLevel is high enough and category does not filter it out
	// a msgLevelShow of > 5 limits the output to only critical messages
	def showLogs(showCategory: String = "ALL", msgLevel: Int = 3, clearLogs: Boolean = false): Unit = {
		for (logMsg <- logList) {
			if (logMsg.logLevel >= msgLevel) {
				if ((showCategory == "ALL") || (logMsg.msgCategory == showCategory) || logCategories(showCategory)) {
					println(s"Log: $logMsg")
				}
			}
		}

		if (clearLogs) {
			clearLogMessages()
		}
	}

	// clear all the logged messages
	private def clearLogMessages(): Unit = {
		logList.clear()
	}

	// Create Log Message storing text message, its category and log level
	private class LogMsg(val msg: String, val msgCategory: String = "MAIN", val logLevel: Int) {
		override def toString: String = s"$msgCategory: [$logLevel] $msg"
	}
}
