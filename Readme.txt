
William Hill Coding Challenge - Deposit Location application

This is a console application will execute and test the Deposit Locations application.
The first argument on the command line is the name of the input text file.
The input file consists of:
  A. a list of tuples: pairs of waypoints or waypoint and deposit location pairs with travel time distance between them
  B. a list of pairs of user names and starting locations

Assumptions:
  Usernames are any non-numeric single element name starting with an letter
  Travel times are all integers
  Input tuples of waypoints, deposit locations and travel times cannot begin with deposit location first
  Invalid node names or other format errors invalid the single input line - all other valid data is processed
  Two waypoints or a waypoint and deposit location with a travel time of 0 will be ignored
  Two waypoints or a waypoint and deposit location that matches a previous entry will overwrite the travel time

Program Flow:
  1. Load and parse the input data (DataService)
        The program attempts to load from the file name provide as the first argument on the command line
        If that file is invalid or not found, or if the file name of the input text file is omitted,
          the program attempts to load from the TestData.txt file
        If the TestData.txt file cannot be loaded, then an error message is shown and hard-coded sample data is used
        Once the test data is loaded, the data is parsed -
        Note: If errors are found during parsing, then an error message is displayed and the input is ignored

  2. Build the connection paths as a two-dimensional grid (PathwaysGrid object)
		The tuples of nodes and travel times are passed to the PathwaysGrid object to build the bi-directional graph
		  representing the waypoints nodes and the deposit locations nodes along with the travel times between each
		Note: The PathwaysGrid code is designed so that if it were desired, new data could be passed with updated times
		  so that the grid could be modified and new (possibly different) paths can be generated

  3. Generate solution paths for each user and starting point pair (LocatorService)
		For each of the username starting location pairs:
		  The shortest path to a deposit location is determined by traversing each edge the node is connected to
		  This process is repeated recursively until a deposit location is found or the travel time exceeds an already
		    established best time for another travel path - if the new path is shorter, the new path and time is saved
		  Once the best path (shortest time) is established, it is returned for display
		  Note: An InvalidDataException is thrown if any errors are found with User Data and an error message is displayed

  4. display user name, waypoints path, deposit location and total time distance
        The best path is displayed as standard output as shown:
          <username> <node 1> <node 2> <node 3> <total travel time>
