package it.polito.dp2.RNS.sol2;


public class PathFinderFactory {
	

	public PathFinder newPathFinder() throws PathFinderException {
		
		// check if the system property is properly setted
		if(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL") == null){
			System.err.println("the system property  `it.polito.dp2.RNS.lab3.Neo4JURL` is null");
			throw new PathFinderException();
		}
		
		// create a new path finder
		PathFinder f = new PathFinder(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL"));
		return f;
		
	}

}