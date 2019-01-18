package it.polito.dp2.RNS.sol2;


public class PathFinderFactory {
	

	public PathFinder newPathFinder() throws PathFinderException {
		String prop;
		// check if the system property is properly setted
		// otherwise use the default value
		if(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL") == null)
			prop = System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL");
		else
			prop = "http://localhost:7474/db";
		
		// create a new path finder
		PathFinder f = new PathFinder(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL"));
		return f;
		
	}

}