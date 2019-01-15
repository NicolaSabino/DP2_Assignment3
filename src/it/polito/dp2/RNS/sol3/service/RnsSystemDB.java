package it.polito.dp2.RNS.sol3.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

	private static RnsSystemDB 									db = new RnsSystemDB();		// creation of singleton
	private static RnsReader									monitor;					// monitor to access the RNS interfaces
	private static Map<String, Place>							places;
	private static Map<String, Place>							gates;
	private static Map<String, Place>							parkingAreas;
	private static Map<String, Place>							roadSegments;
	private static Map<String, Vehicle>							vehicles;
	private static Map<String,ConcurrentSkipListSet<String>>	paths;
	private static Map<String,Integer>							capacityInPlace;
	private static Map<String,ConcurrentLinkedQueue<String>>	vehiclesInPlace;
	private static Queue<Connection>							connections;
	private static PathFinder									pathFinder;
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
		paths = new ConcurrentHashMap<String,ConcurrentSkipListSet<String>>();
		capacityInPlace = new ConcurrentHashMap<>();
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
			capacityInPlace.put(tmp.getId(),new Integer(0));	// create new place in `capacityInPlace`
			vehiclesInPlace.put(tmp.getId(), new ConcurrentLinkedQueue<String>());	//create a new place in `vehiclesInPlace`
			paths.put(tmp.getId(), new ConcurrentSkipListSet<String>());	// create a new place in `paths`
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
			capacityInPlace.put(tmp.getId(),new Integer(0));		// create new place in `capacityInPlace`
			vehiclesInPlace.put(tmp.getId(), new ConcurrentLinkedQueue<String>());	//create a new place in `vehiclesInPlace`
			paths.put(tmp.getId(), new ConcurrentSkipListSet<String>());	// create a new place in `paths`
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
			capacityInPlace.put(tmp.getId(),new Integer(0));		// create new place in `capacityInPlace`
			vehiclesInPlace.put(tmp.getId(), new ConcurrentLinkedQueue<String>());	//create a new place in `vehiclesInPlace`
			paths.put(tmp.getId(), new ConcurrentSkipListSet<String>());	// create a new place in `paths`

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
		ConcurrentLinkedQueue<String> queue = vehiclesInPlace.get(vehicle.getOrigin());
		queue.add(vehicle.getId());
		return vehicles.putIfAbsent(vehicle.getId(), vehicle);
	}
	

	public Vehicle updateVehicleState(String id,Vehicle vehicle) {
		Vehicle v = vehicles.get(id);			// select the target vehicle
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

	public Collection<String> storeShortesPath(String id, List<String> res) {
		return paths.putIfAbsent(id, new ConcurrentSkipListSet<String>(res));
	}
	
	public Integer getCapacity(String place){
		return capacityInPlace.get(place);
	}

	public List<Place> calculatePath(Vehicle vehicle, Place place) {
		// TODO Auto-generated method stub
		return null;
	}

	public void incrementPlace(String id) {
		Integer capacity = capacityInPlace.get(id);
		capacityInPlace.put(id, capacity.intValue()+1);
	}
	
	public void decrementPlace(String id) {
		Integer capacity = capacityInPlace.get(id);
		capacityInPlace.put(id, capacity.intValue()-1);
	}

	public Collection<String> getVehiclesFromPlace(String id) {
		return vehiclesInPlace.get(id);
	}

	public ConcurrentSkipListSet<String> getShortestPath(String id) {
		return paths.get(id);
	}

	public Vehicle setNewPosition(String vehicle,String place) {
		Vehicle v = vehicles.get(vehicle);
		v.setPosition(place);
		return vehicles.put(vehicle, v);
	}

	public Object clearShortestPath(String vehicle) {
		Object o = paths.remove(vehicle);
		paths.put(vehicle, new ConcurrentSkipListSet<>());
		return o;
	}
}
