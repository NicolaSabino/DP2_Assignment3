package it.polito.dp2.RNS.sol3.admClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.sol1.jaxb.Connection;
import it.polito.dp2.RNS.sol1.jaxb.Connections;
import it.polito.dp2.RNS.sol1.jaxb.Gate;
import it.polito.dp2.RNS.sol1.jaxb.ParkingArea;
import it.polito.dp2.RNS.sol1.jaxb.Place;
import it.polito.dp2.RNS.sol1.jaxb.Places;
import it.polito.dp2.RNS.sol1.jaxb.RnsSystem;
import it.polito.dp2.RNS.sol1.jaxb.RoadSegment;
import it.polito.dp2.RNS.sol1.jaxb.Vehicle;
import it.polito.dp2.RNS.sol1.jaxb.Vehicles;

public class UtilityMap {
	
	private WebTarget target;
	public Map<String,IdentifiedEntityReader_> i_map;
	public NavigableMap<String,PlaceReader_> p_map;
	public Map<String,RoadSegmentReader_> rs_map;
	public Map<String,ParkingAreaReader_> pa_map;
	public Map<String,GateReader_> g_map;
	public Map<String,VehicleReader_> v_map;
	public Map<String,String> p_link_map;
	public Map<String,String> rs_link_map;
	public Map<String,String> pa_link_map;
	public Map<String,String> g_link_map;
	public Map<String,String> c_link_map;
	public List<ConnectionReader_> c_list;
	public Map<String,URI> resources;
	  
	  /**
	   * Constructor
	   * @param rns
	   */
	  public UtilityMap(WebTarget target) {
		  
		// If we work with `TreeMap` we can use benefits of underlying tree data structure,
		// and search with prefix with a O(log(N)) complexity.
		// If we work with `HashMap` we can easily retrieve objects with the key 
		// with a O(1) complexity
		// Both `TreeMap` and `HashMap` have the same `Map` interface.
		this.target = target;
		this.p_map = new TreeMap<>();
		this.rs_map = new HashMap<>();
		this.pa_map = new HashMap<>();
		this.i_map = new HashMap<>();
		this.v_map = new HashMap<>();
		this.g_map = new HashMap<>();
		this.p_link_map = new HashMap<>();
		this.rs_link_map = new HashMap<>();
		this.pa_link_map = new HashMap<>();
		this.g_link_map = new HashMap<>();
		this.c_list = new ArrayList<>();
		this.resources = new HashMap<>();
		
		RnsSystem system = null;
		
		try {
			system = this.getRoot();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.resources.put("places", UriBuilder.fromUri(system.getPlaces()).build());
		this.resources.put("connections", UriBuilder.fromUri(system.getConnections()).build());
		this.resources.put("vehicles", UriBuilder.fromUri(system.getVehicles()).build());
		
		Response p_res = null;
		Response c_res = null;
		
		try {
			p_res = this.getPlaces();
			c_res = this.getConnections();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Places places = p_res.readEntity(Places.class);
		Connections connections = c_res.readEntity(Connections.class);
		
		// PLACES
		for(Place p :places.getPlace()){	// for each place in the system
			IdentifiedEntityReader_ entity = new IdentifiedEntityReader_(p.getId());	// create a new entity
			PlaceReader_ place = new PlaceReader_(entity,p.getCapacity());				// create a new PlaceReader
			if(p.getRoadSegment() != null){ 									// if the place is a ROAD SEGMENT
				RoadSegment rst = p.getRoadSegment();							
				RoadSegmentReader_ rs = new RoadSegmentReader_(place, rst.getName(), rst.getRoad());	// create a RoadSegmentReader
				this.rs_map.put(p.getId(),rs);															// store the RoadSegmentReader
				this.rs_link_map.put(p.getSelf(),p.getId());
			}
			if(p.getParkingArea() != null){										// if the place is a PARKING AREA
				ParkingArea pat = p.getParkingArea();
				Set<String> services = new HashSet<>(pat.getService());									// convert a list into a set
				ParkingAreaReader_ pa = new ParkingAreaReader_(place, services);						// create the ParkingAreaReader
				this.pa_map.put(p.getId(), pa);															// store the ParkingAreaReader
				this.pa_link_map.put(p.getSelf(), p.getId());
			}
			if(p.getGate() != null){											// if the place is a GATE
				Gate gt = p.getGate();																
				GateType_ gtt = GateType_.valueOf(gt.toString());										// conversion
				GateReader_ g = new GateReader_(place, gtt);											// create the GateReader
				this.g_map.put(p.getSelf(),g);															// store the GateReader
				this.g_link_map.put(p.getSelf(), p.getId());
			}
			
			this.p_map.put(p.getId(),place);	// store the PlaceReader
			this.i_map.put(p.getId(),entity);	// store the IdentifiedEntityReader
			this.p_link_map.put(p.getSelf(),p.getId());
		}
		
		// CONNECTIONS
		for(Connection c: connections.getConnection()){ // for each connection in the system
			String f = this.p_link_map.get(c.getFrom());							// convert the `from` link in a valid identifier
			String t = this.p_link_map.get(c.getTo());								// convert the `to` link in a valid identifier
			PlaceReader_ from = this.p_map.get(f);									// set the `from` in the p_map
			PlaceReader_ to = this.p_map.get(t);									// set the `to` in the p_map
			ConnectionReader_ connection = new ConnectionReader_(from, to);			// create a new ConnectionReader
			this.c_list.add(connection);											// store the connection reader
		}
		
		// NEXT PLACES
		for(Place p : places.getPlace()){ // for each place
			PlaceReader_ pp = this.p_map.get(p.getId()); //get the corresponding object in the map
			for(String uri: p.getNextPlace()){	// for each next place
				String identifier = this.p_link_map.get(uri);		// convert the uri in a valid place
				PlaceReader_ nexthop = this.p_map.get(identifier);	// find the corresponding object place in the map 
				pp.addNextPlace(nexthop); 							// make the connection
			}
		}
			
		
		//creating read-only collections
        this.p_map = Collections.unmodifiableNavigableMap(this.p_map);
        this.rs_map = Collections.unmodifiableMap(this.rs_map);
        this.pa_map = Collections.unmodifiableMap(this.pa_map);
        this.g_map = Collections.unmodifiableMap(this.g_map);
        this.p_link_map = Collections.unmodifiableMap(this.p_link_map);
        this.rs_link_map = Collections.unmodifiableMap(this.p_link_map);
        this.pa_link_map = Collections.unmodifiableMap(this.p_link_map);
        this.g_link_map = Collections.unmodifiableMap(this.g_link_map);
        this.c_list = Collections.unmodifiableList(this.c_list);

	}
	  
  /**
	 * This method is able to get a portion of map using a prefix
	 * with a O(log(N)) complexity
	 * @param prefix
	 * @return Pruned map
	 */
	public SortedMap<String, PlaceReader_> getByPrefix( String prefix ) {
	        return this.p_map.subMap( prefix, prefix + Character.MAX_VALUE );
	}
	
	/**
	 * This method check if a  generic set is contained into another one
	 * @param setA - Set
	 * @param setB - SubSet
	 * @return true if so, false otherwise
	 */
	public <T> boolean isSubset(Set<T> setA, Set<T> setB) {
	    return setB.containsAll(setA);
	  }
	
	/**
	 * Get all places
	 * Syntax:		GET http://localhost:8080/RnsSystem/rest/rns/places
	 * @return Response
	 * @throws Exception
	 */
	private RnsSystem getRoot() throws Exception{
		Response result;
		try{
			result = this.target.path("rns").request(MediaType.APPLICATION_XML).get(Response.class);
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return result.readEntity(RnsSystem.class);
	}
	  
	/**
	 * Get all places
	 * Syntax:		GET http://localhost:8080/RnsSystem/rest/rns/places
	 * @return Response
	 * @throws Exception
	 */
	 private Response getPlaces() throws Exception{
		 Response result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("places"));
			result = target.request(MediaType.APPLICATION_XML).get(Response.class);
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return result;
	 }
	 
	 /**
	 * Get all connections
	 * Syntax:		GET http://localhost:8080/RnsSystem/rest/rns/connections
	 * @return Response
	 * @throws Exception
	 */
	 private Response getConnections() throws Exception{
		 Response result;
			try{
				WebTarget target = ClientBuilder.newClient().target(this.resources.get("connections"));
				result = target.request(MediaType.APPLICATION_XML).get(Response.class);
					if(result == null)
						throw new Exception();
			}catch (Exception e) {
				e.printStackTrace();
				throw new Exception();
			}
			return result;
	 }
	 
	 /**
	 * Get a vehicle
	 * Syntax:		GET http://localhost:8080/RnsSystem/rest/rns/vehicles/{id}
	 * @param plate id
	 * @return Response
	 * @throws Exception
	 */
	 private Response getVehicle_(String id) throws Exception{
			Response result;
			try{
				WebTarget target = ClientBuilder.newClient().target(this.resources.get("vehicles"));
				result = target.path(id).request(MediaType.APPLICATION_XML).get(Response.class);
					if(result == null)
						throw new Exception();
			}catch (Exception e) {
				e.printStackTrace();
				throw new Exception();
			}
			return result;
		}
	
	 /**
	 * Get all vehicles
	 * Syntax:		GET http://localhost:8080/RnsSystem/rest/rns/vehicles
	 * @return Response
	 * @throws Exception
	 */
	private Response getVehicles_() throws Exception{
		Response result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("vehicles"));
			result = target.request(MediaType.APPLICATION_XML).get(Response.class);
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return result;
	}
	
	/**
	 * Get all connections
	 * Syntax:		GET http://localhost:8080/RnsSystem/rest/rns/places/{id}/vehicles
	 * @param place uri resource
	 * @return Response
	 * @throws Exception
	 */
	private Response getVehicles_(String place) throws Exception{
		Response result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("places"));
			result = target.path(place).path("vehicles").request(MediaType.APPLICATION_XML).get(Response.class);
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return result;
	}
	
	// ok
	public VehicleReader getVehicle(String id) throws Exception {
		System.out.println("Get Vehicle " + id);
		Response r = this.getVehicle_(id);
		Vehicle v = r.readEntity(Vehicle.class);
		IdentifiedEntityReader_ entity = new IdentifiedEntityReader_(v.getId());	// create a new entity
		String pos = this.p_link_map.get(v.getPosition());							// convert position link to a a valid place
		String ori = this.p_link_map.get(v.getPosition());							// convert origin link to a a valid place
		String des = this.p_link_map.get(v.getPosition());							// convert destination link to a a valid place
		PlaceReader_ position = this.p_map.get(pos);								// set the position in p_map
		PlaceReader_ origin = this.p_map.get(ori);									// set the origin in p_map
		PlaceReader_ destination = this.p_map.get(des);								// set the destination in p_map
		Calendar entryTime = CalendarConverter.toCalendar(v.getEntryTime());		// convert the entryTime
		VehicleState_ state = VehicleState_.valueOf(v.getState().toString());		// convert the state
		VehicleType_ type = VehicleType_.valueOf(v.getType().toString());			// convert the type
		VehicleReader_ vehicle = new VehicleReader_(entity, entryTime, origin, position, destination, state, type);	// create a vehicle reader
		return vehicle;
		
	}
	
	public Set<VehicleReader> getVehicles() throws Exception {
		Response r = this.getVehicles_();
		Vehicles vv = r.readEntity(Vehicles.class);
		Set<VehicleReader> set = new HashSet<>();
		for(Vehicle v:vv.getVehicle()){
			IdentifiedEntityReader_ entity = new IdentifiedEntityReader_(v.getId());	// create a new entity
			String pos = this.p_link_map.get(v.getPosition());							// convert position link to a a valid place
			String ori = this.p_link_map.get(v.getPosition());							// convert origin link to a a valid place
			String des = this.p_link_map.get(v.getPosition());							// convert destination link to a a valid place
			PlaceReader_ position = this.p_map.get(pos);								// get the position in p_map
			PlaceReader_ origin = this.p_map.get(ori);									// get the origin in p_map
			PlaceReader_ destination = this.p_map.get(des);								// get the destination in p_map
			Calendar entryTime = CalendarConverter.toCalendar(v.getEntryTime());		// convert the entryTime
			VehicleState_ state = VehicleState_.valueOf(v.getState().toString());		// convert the state
			VehicleType_ type = VehicleType_.valueOf(v.getType().toString());			// convert the type
			VehicleReader_ vehicle = new VehicleReader_(entity, entryTime, origin, position, destination, state, type);	// create a vehicle reader
			set.add(vehicle);
		}
		return set;
		
	}
	
	public Set<VehicleReader> getVehicles(String id) throws Exception {
		Response r = this.getVehicles_(id);
		Vehicles vv = r.readEntity(Vehicles.class);
		Set<VehicleReader> set = new HashSet<>();
		for(Vehicle v:vv.getVehicle()){
			IdentifiedEntityReader_ entity = new IdentifiedEntityReader_(v.getId());	// create a new entity
			String pos = this.p_link_map.get(v.getPosition());							// convert position link to a a valid place
			String ori = this.p_link_map.get(v.getPosition());							// convert origin link to a a valid place
			String des = this.p_link_map.get(v.getPosition());							// convert destination link to a a valid place
			PlaceReader_ position = this.p_map.get(pos);								// get the position in p_map
			PlaceReader_ origin = this.p_map.get(ori);									// get the origin in p_map
			PlaceReader_ destination = this.p_map.get(des);								// get the destination in p_map
			Calendar entryTime = CalendarConverter.toCalendar(v.getEntryTime());		// convert the entryTime
			VehicleState_ state = VehicleState_.valueOf(v.getState().toString());		// convert the state
			VehicleType_ type = VehicleType_.valueOf(v.getType().toString());			// convert the type
			VehicleReader_ vehicle = new VehicleReader_(entity, entryTime, origin, position, destination, state, type);	// create a vehicle reader
			set.add(vehicle);
		}
		return set;
		
	}
	
	

}
