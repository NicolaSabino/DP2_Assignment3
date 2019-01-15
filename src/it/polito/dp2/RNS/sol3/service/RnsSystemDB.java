package it.polito.dp2.RNS.sol3.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.ws.rs.InternalServerErrorException;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.FactoryConfigurationError;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.sol1.jaxb.Connection;
import it.polito.dp2.RNS.sol1.jaxb.Gate;
import it.polito.dp2.RNS.sol1.jaxb.ParkingArea;
import it.polito.dp2.RNS.sol1.jaxb.Place;
import it.polito.dp2.RNS.sol1.jaxb.Places;
import it.polito.dp2.RNS.sol1.jaxb.RoadSegment;
import it.polito.dp2.RNS.sol1.jaxb.ShortestPath;
import it.polito.dp2.RNS.sol1.jaxb.Vehicle;
import it.polito.dp2.RNS.sol2.BadStateException;
import it.polito.dp2.RNS.sol2.ModelException;
import it.polito.dp2.RNS.sol2.PathFinder;
import it.polito.dp2.RNS.sol2.PathFinderException;
import it.polito.dp2.RNS.sol2.PathFinderFactory;
import it.polito.dp2.RNS.sol2.ServiceException;
import it.polito.dp2.RNS.sol2.UnknownIdException;

// SINGLETON  CLASS FILE
public class RnsSystemDB {

	private static RnsSystemDB 								db = new RnsSystemDB();		// creation of singleton
	private static RnsReader								monitor;					// monitor to access the RNS interfaces
	private static Map<String, Place>						places;
	private static Map<String, Place>						gates;
	private static Map<String, Place>						parkingAreas;
	private static Map<String, Place>						roadSegments;
	private static Map<String, Vehicle>						vehicles;
	private static Map<String,ShortestPath>					paths;
	private static Map<String,Integer>						vehiclesInPlace;
	private static Queue<Connection>						connections;
	private static PathFinder								pathFinder;
	//Queue<String> globalQueue = new ConcurrentLinkedQueue<String>();
	/**
	 * Private constructor of the singleton
	 */
	private RnsSystemDB(){
		
		PathFinderFactory factory = new PathFinderFactory();			// create a new path finder factory
		try {
			pathFinder = factory.newPathFinder();						// create a new path finder
		} catch (FactoryConfigurationError | PathFinderException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}
		
		// try to load the model, a monitor is already initialized in `path finder`
		try {
			monitor = pathFinder.reloadModel();				// load the model and return the monitor
		} catch (ServiceException | ModelException e) {
			e.printStackTrace();
			throw new InternalServerErrorException();
		}
		
		
		
		places = new ConcurrentHashMap<String,Place>();
		gates = new ConcurrentHashMap<String,Place>();
		parkingAreas = new ConcurrentHashMap<String,Place>();
		roadSegments = new ConcurrentHashMap<String,Place>();
		vehicles = new ConcurrentHashMap<String,Vehicle>();
		connections = new ConcurrentLinkedQueue<Connection>();
		paths = new ConcurrentHashMap<String,ShortestPath>();
		vehiclesInPlace = new ConcurrentHashMap<>();
		
		
		for(GateReader g:monitor.getGates(null)){				// for each gate
			Place	tmp = new Place();							// create an empty place container
			tmp.setId(g.getId());								// set `id` field
			tmp.setCapacity(g.getCapacity());					// set `capacity` field
			tmp.setGate(Gate.valueOf(g.getType().toString()));	// set `gate` field
			tmp.setSelf(null);	// set the `self` field
			for(PlaceReader r:g.getNextPlaces())				// for each next place
				tmp.getNextPlace().add(r.getId()); // add the URI			
			places.put(tmp.getId(),tmp);						// add the container to `places`
			gates.put(tmp.getId(), tmp);						// add the container to `gates`
			vehiclesInPlace.put(tmp.getId(),new Integer(0));	// create new place in `vehiclesInPlace`
		}
		
		
		for(ParkingAreaReader pa:monitor.getParkingAreas(null)){	// for each parking area
			Place tmp = new Place();								// create an empty place container
			ParkingArea tmp2 = new ParkingArea();					// create an empty parking area container
			tmp.setId(pa.getId());									// set `id` field
			tmp.setCapacity(pa.getCapacity());						// set `capacity`
			tmp2.getService().addAll(pa.getServices());				// set the list of services
			tmp.setParkingArea(tmp2);								// put the parking area container in the place container
			tmp.setSelf(null); 										// set the `self` field
			for(PlaceReader r:pa.getNextPlaces())					// for each next place
				tmp.getNextPlace().add(r.getId()); 					// add the URI	
			places.put(tmp.getId(), tmp);							// add the container to  `places`	
			parkingAreas.put(tmp.getId(), tmp);						// add the container to `parkingAreas`
			vehiclesInPlace.put(tmp.getId(),new Integer(0));		// create new place in `vehiclesInPlace`
		}
		
		
		for(RoadSegmentReader rs:monitor.getRoadSegments(null)){	// for each road segment
			Place tmp = new Place();								// create an empty place container
			RoadSegment tmp2 = new RoadSegment();					// create an empty road segment container
			tmp.setId(rs.getId());									// set `id` field
			tmp.setCapacity(rs.getCapacity());						// set `capacity` field
			tmp2.setName(rs.getName());								// set `Name` field
			tmp2.setRoad(rs.getRoadName());							// set `RoadName` field
			tmp.setRoadSegment(tmp2);								// put the road segment container in the place container
			tmp.setSelf(null); 										// set the `self` field
			for(PlaceReader r:rs.getNextPlaces())					// for each next place
				tmp.getNextPlace().add(r.getId()); 					// add the URI	
			places.put(tmp.getId(), tmp);							// add the container to `places`
			roadSegments.put(tmp.getId(), tmp);						// add the container to `roadSegments`
			vehiclesInPlace.put(tmp.getId(),new Integer(0));		// create new place in `vehiclesInPlace`

		}
		
		for(ConnectionReader c:monitor.getConnections()){				// for each connection
			Connection tmp = new Connection();							// create an empty connection container
			tmp.setFrom(c.getFrom().getId());	// set `from` field
			tmp.setTo(c.getTo().getId());		// set `to` filed
			connections.add(tmp);										// add the container to `connections`
		}
	}
	
	/**
	 * It allows to access externally to the singleton 
	 * @return db
	 */
	public static RnsSystemDB getDB() {
		return db;
	}
	
	
	
	public Collection<Place> getPlaces(SearchScope scope) {
		switch(scope) {
		case ALL: return getPlaces(RnsSystemDB.places);
		case GATES: return getPlaces(RnsSystemDB.gates);
		case ROADSEGMENTS: return getPlaces(RnsSystemDB.roadSegments);
		case PARKINGAREAS: return getPlaces(RnsSystemDB.parkingAreas);
		default: return null;
	}
	}
	
	public Collection<Place> getPlaces(Map<String, Place> map) {
		return new ConcurrentLinkedQueue<>(map.values());	// return only the values of the selected map
	}
	
	public Place getPlace(String id){
		Place place = places.get(id); //if present it return the place, otherwise null
		return place;
	}

	public Places getPlacesConnectedTo(String id) {
		Place place = places.get(id);		// search the place in the db
		Places places = new Places();		// create a new empty container
		if (place == null) return null;		// if the palce does not exists return null
		for(String identifier:place.getNextPlace()){		// for each identifier in `nextplaces`
			Place p = RnsSystemDB.places.get(identifier);	// get the place
			places.getPlace().add(p);						// put it in the container
		}
		return places;
	}

	public Collection<Vehicle> getVehicles() {
		return new ConcurrentLinkedQueue<>(RnsSystemDB.vehicles.values());
	}

	public Vehicle getVehicle(String id) {
		Vehicle vehicle = vehicles.get(id);
		return vehicle;
	}

	public Vehicle createVehicle(Vehicle vehicle) {
		return vehicles.putIfAbsent(vehicle.getId(), vehicle);
	}
	

	public Vehicle updateVehicleState(Vehicle vehicle) {
		Vehicle v = vehicles.get(vehicle.getId());			// select the target vehicle
		if(v==null) return null;							// if the vehicle does not exist return null
		v.setState(vehicle.getState()); 					// update the state
		return v;											// return the update instance of the vehicle
	}

	// TODO  manage the path
	public Vehicle updateVehiclePosition(Vehicle vehicle) {
		Vehicle v = vehicles.get(vehicle.getId());			// select the target vehicle
		if(v==null) return null;							// if the vehicle does not exist return null
		v.setPosition(vehicle.getPosition()); 				// update the position
		return v;
	}

	public boolean deleteVehicle(String id) {
		if(!vehicles.containsKey(id)) return false;
		vehicles.remove(id);
		return true;
	}

	public Collection<Connection> getConnections() {
		return connections;
	}

	public List<String> isReachable(String from,String to) {
		Set<List<String>> resultSet = null;
		try {
			resultSet = pathFinder.findShortestPaths(from, to, 1000);
		} catch (UnknownIdException | BadStateException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator<List<String>> iter = resultSet.iterator();
		return iter.next();
	}

	public ShortestPath storeShortesPath(String id, ShortestPath path) {
		return paths.putIfAbsent(id, path);
	}
	
	public Integer manageCapacity(String place){
		return vehiclesInPlace.get(place);
	}

	public List<Place> calculatePath(Vehicle vehicle, Place place) {
		// TODO Auto-generated method stub
		return null;
	}

	public void incrementPlace(String id) {
		Integer capacity = vehiclesInPlace.get(id);
		vehiclesInPlace.put(id, capacity.intValue()+1);
	}
	
	public void decrementPlace(String id) {
		Integer capacity = vehiclesInPlace.get(id);
		vehiclesInPlace.put(id, capacity.intValue()-1);
	}
}
