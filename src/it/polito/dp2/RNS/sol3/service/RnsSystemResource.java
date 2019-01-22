package it.polito.dp2.RNS.sol3.service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol1.jaxb.Connection;
import it.polito.dp2.RNS.sol1.jaxb.Connections;
import it.polito.dp2.RNS.sol1.jaxb.Gate;
import it.polito.dp2.RNS.sol1.jaxb.ParkingArea;
import it.polito.dp2.RNS.sol1.jaxb.Place;
import it.polito.dp2.RNS.sol1.jaxb.Places;
import it.polito.dp2.RNS.sol1.jaxb.RnsSystem;
import it.polito.dp2.RNS.sol1.jaxb.RoadSegment;
import it.polito.dp2.RNS.sol1.jaxb.ShortestPath;
import it.polito.dp2.RNS.sol1.jaxb.VState;
import it.polito.dp2.RNS.sol1.jaxb.Vehicle;
import it.polito.dp2.RNS.sol1.jaxb.Vehicles;
import io.swagger.annotations.ApiResponse;

@Path("/rns") // main path
@Api(value = "/rns")
// RESOURCE CLASS FILE
public class RnsSystemResource {

	//ATTRIBUTES
	public UriInfo			uriInfo;
	public RnsSystemService	service = new RnsSystemService(); // a service object is create for each RnsSystem resource
	
	public RnsSystemResource(@Context UriInfo uriInfo) {
		this.uriInfo = uriInfo; //injection of the context information
	}
	
	/*
	 * 6. An administrator must be able to read all the information available in DP2-RNS about
	 * places and tracked vehicles in the system. 
	 * The information to be made available for reading is the same that was defined for Assignment 1,
	 * plus information about the suggested path for each tracked vehicle in the system.
	 * The administrator should be able to easily get the
	 * information about the vehicles that are in a given place.
	 */
	
	@GET
    @ApiOperation(value = "getRnsSystem", notes = "read the `RnsSystem` object")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK ")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public RnsSystem getRnsSystem(){
		RnsSystem sys = new RnsSystem();													// create an empty container
		UriBuilder root = uriInfo.getAbsolutePathBuilder();									// get the absolute path of the request.
		sys.setSelf(root.toTemplate());														// set the `self` field
		sys.setPlaces(root.clone().path("places").toTemplate());							// set `places` field
		sys.setGates(root.clone().path("places").path("gates").toTemplate());				// set the `gates` field
		sys.setRoadSegments(root.clone().path("places").path("roadSegments").toTemplate());	// set the `RoadSegments" field
		sys.setParkingAreas(root.clone().path("places").path("parkingAreas").toTemplate());	// set the `parlingAreas" field
		sys.setConnections(root.clone().path("connections").toTemplate());					// set the `connections" field
		sys.setVehicles(root.clone().path("vehicles").toTemplate());						// set the `vehicles" field
		return sys; // 200 OK
	}
	
	@GET
	@Path("/places")
    @ApiOperation(value = "getPlaces", notes = "read the `Places` object")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getPlaces(){
		// be careful with POINTER when accessing directly to DB data structures.
		// we have to create a CLONE in order to not modify the DB resource
		Places places = service.getPlaces(SearchScope.ALL);		// get all the places from the service
		UriBuilder root = uriInfo.getAbsolutePathBuilder();		// get the root URI
		Places places2 = new Places();							// new EMPTY places container (the final clone)
		for(Place p:places.getPlace()){															// for each place
			Place temp = new Place();															// create a new empty place container
			temp.setId(p.getId());																// set the `id` field
			temp.setCapacity(p.getCapacity());													// set the `capacity` field
			temp.setSelf(root.clone().path(p.getId()).toTemplate());							// set the `self` field
			temp.setConnectedTo(root.clone().path(p.getId()).path("connectedTo").toTemplate());	// set the `connectedTo` field
			temp.setVehicles(root.clone().path(p.getId()).path("vehicles").toTemplate());
			// check the type of place
			if(p.getGate()!= null){
				temp.setGate(p.getGate());															// set the `gate` fields
			}else if(p.getRoadSegment()!= null){
				RoadSegment rs = new RoadSegment();													// create a new empty road segment
				rs.setName(p.getRoadSegment().getName());											// set `name`
				rs.setRoad(p.getRoadSegment().getRoad());											// set `road`
				temp.setRoadSegment(rs);			
			}else if(p.getParkingArea()!= null){
				ParkingArea pa = new ParkingArea();													// create a new empty parking area
				pa.getService().addAll(p.getParkingArea().getService());							// set `services`
				temp.setParkingArea(pa);
			}else{
				throw new InternalServerErrorException();
			}
			for(String identifier:p.getNextPlace())												// for each next place
				temp.getNextPlace().add(root.clone().path(identifier).toTemplate());			// set the `nextplace` link
			places2.getPlace().add(temp);														// add the place to places2														
		}
		
		return places2; // 200 OK
	}
	
	@GET
	@Path("/connections")
    @ApiOperation(value = "getConnections", notes = "read the `Connections` object")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Connections getConnections(){
		Connections connections = new Connections();
		UriBuilder root = uriInfo.getAbsolutePathBuilder();								// get the root URI
		String uri = root.clone().toTemplate();											// create an uri string
		for(Connection c:service.getConnections().getConnection()){						// for each connection
			Connection temp = new Connection();											// create an empty connection
			temp.setFrom(uri.replace("connections", "places/".concat(c.getFrom())));	// set `from` field
			temp.setTo(uri.replace("connections", "places/".concat(c.getTo())));		// set `to` field
			connections.getConnection().add(temp);										// add the connection to the empty set
		}
		return connections; // 200 OK
	}
	
	@GET
	@Path("/places/gates")
    @ApiOperation(value = "getGates", notes = "read the `Place` object with `Gate` elements only")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getGates(@QueryParam("type") String keyword){
		Places places = service.getPlaces(SearchScope.GATES);		// get all the places from the service
		UriBuilder root = uriInfo.getAbsolutePathBuilder();		// get the root URI
		Places places2 = new Places();							// new EMPTY places container (the final clone)
		for(Place p:places.getPlace()){															// for each place
			Place temp = new Place();															// create a new empty place container
			temp.setId(p.getId());																// set the `id` field
			temp.setCapacity(p.getCapacity());													// set the `capacity` field
			String s = root.clone().toTemplate();
			temp.setSelf(s.replace("gates", p.getId()));										// set the `self` field
			temp.setConnectedTo(s.replace("gates", p.getId().concat("/connectedTo")));			// set the `connectedTo` field
			temp.setVehicles(s.replace("gates", p.getId().concat("/vehicles")));				// set the `vehicles` field
			temp.setGate(p.getGate());															// set the `gate` fields
			for(String identifier:p.getNextPlace()){											// for each next place
				String ss = root.clone().toTemplate();
				temp.getNextPlace().add(ss.replace("gates", identifier));						// set the `nextplace` link
			}
			places2.getPlace().add(temp);														// add the place to places2														
		}
		return places; // 200 OK
	} 
	
	@GET 
	@Path("/places/roadSegments")
    @ApiOperation(value = "getRoadSegments", notes = "read the `Place` object with `RoadSegment` elements only")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getRoadSegments(@QueryParam("type") String keyword){
		Places places = service.getPlaces(SearchScope.ROADSEGMENTS);		// get all the places from the service
		UriBuilder root = uriInfo.getAbsolutePathBuilder();					// get the root URI
		Places places2 = new Places();										// new EMPTY places container (the final clone)
		for(Place p:places.getPlace()){															// for each place
			Place temp = new Place();															// create a new empty place container
			temp.setId(p.getId());																// set the `id` field
			temp.setCapacity(p.getCapacity());													// set the `capacity` field
			String s = root.clone().toTemplate();
			temp.setSelf(s.replace("roadSegments",p.getId()));									// set the `self` field
			temp.setConnectedTo(s.replace("roadSegments", p.getId().concat("/connectedTo")));	// set the `connectedTo` field
			temp.setVehicles(s.replace("roadSegments",p.getId().concat("/vehicles")));			// set the `vehicles`
			RoadSegment rs = new RoadSegment();													// create a new empty road segment
			rs.setName(p.getRoadSegment().getName());											// set `name`
			rs.setRoad(p.getRoadSegment().getRoad());											// set `road`
			temp.setRoadSegment(rs);															// attach rs to temp
			for(String identifier:p.getNextPlace()){											// for each next place
				String ss = root.clone().toTemplate();
				temp.getNextPlace().add(ss.replace("roadSegments", identifier));				// set the `nextplace` link
			}
			places2.getPlace().add(temp);														// add the place to places2														
		}
		return places; // 200 OK
	}
	
	@GET
	@Path("/places/parkingAreas")
    @ApiOperation(value = "getParkingAreas", notes = "read the `Place` object with `ParkingArea` elements only")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getParkingAreas(@QueryParam("type") String keyword){
		Places places = service.getPlaces(SearchScope.PARKINGAREAS);		// get all the places from the service
		UriBuilder root = uriInfo.getAbsolutePathBuilder();					// get the root URI
		Places places2 = new Places();										// new EMPTY places container (the final clone)
		for(Place p:places.getPlace()){															// for each place
			Place temp = new Place();															// create a new empty place container
			temp.setId(p.getId());																// set the `id` field
			temp.setCapacity(p.getCapacity());													// set the `capacity` field
			String s = root.clone().toTemplate();
			temp.setSelf(s.replace("parkingAreas",p.getId()));									// set the `self` field
			temp.setConnectedTo(s.replace("parkingAreas",p.getId().concat("connectedTo")));		// set the `connectedTo` field
			temp.setVehicles(s.replace("parkingAreas",p.getId().concat("vehicles")));
			ParkingArea pa = new ParkingArea();													// create a new empty parking area
			pa.getService().addAll(p.getParkingArea().getService());							// set `services`
			temp.setParkingArea(pa); 															// attach parking area to tmp
			for(String identifier:p.getNextPlace()){											// for each next place
				String ss = root.clone().toTemplate();
				temp.getNextPlace().add(ss.replace("parkingAreas", identifier));				// set the `nextplace` link
			}
			places2.getPlace().add(temp);														// add the place to places2														
		}
		return places2; // 200 OK
	}
	
	@GET 
	@Path("/places/{id}")
    @ApiOperation(value = "getPlace", notes = "read a single `Place`"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found, the place is not present in the system"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public  Place getPlace(@PathParam("id") String id){
		Place target = service.getPlace(id);											// search the target place with the corresponding `id`
		if (target==null)																// check the return value
			throw new NotFoundException();	// if it is null, the resource does not exists
		
		UriBuilder self = uriInfo.getAbsolutePathBuilder();						// get the absolute path
		Place place = new Place();												// create a new empty place container
		place.setId(target.getId());											// set `id` field
		place.setCapacity(target.getCapacity());								// set `capacity` field
		place.setSelf(self.clone().toTemplate());								// set `self` field
		place.setConnectedTo(self.clone().path("connectedTo").toTemplate());	// set `connectedTo` field
		place.setVehicles(self.clone().path("vehicles").toTemplate());
		// check the type of place
		if(target.getGate()!= null){
			place.setGate(target.getGate());													// set the `gate` fields
		}else if(target.getRoadSegment()!= null){
			RoadSegment rs = new RoadSegment();													// create a new empty road segment
			rs.setName(target.getRoadSegment().getName());										// set `name`
			rs.setRoad(target.getRoadSegment().getRoad());										// set `road`
			place.setRoadSegment(rs);			
		}else if(target.getParkingArea()!= null){
			ParkingArea pa = new ParkingArea();													// create a new empty parking area
			pa.getService().addAll(target.getParkingArea().getService());						// set `services`
			place.setParkingArea(pa);
		}
		for(String identifier:target.getNextPlace()){							// for each next place
			String uri = self.clone().toTemplate();								// get the uri of the current resource
			String newUri = uri.replace(place.getId(), identifier);				// replace the identifier with the proper one
			place.getNextPlace().add(newUri);									// add the new next place
		}
		return target; // return the target place 200 OK
	}
	
	@GET
	@Path("/places/{id}/vehicles")
    @ApiOperation(value = "getVehiclesFromPlace", notes = "read `Vehicles` object in a specific place"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found, the place is not present in the system"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public  Vehicles getVehiclesFromPlace(@PathParam("id") String id){
		if(service.getPlace(id) == null)
			throw new NotFoundException();
		Vehicles vehicles = new Vehicles();									// create a new empty set
		Iterator<String> itr = service.getVehiclesFromPlace(id).iterator();
		while(itr.hasNext()){												// for each vehicle
			String identifier = itr.next();									// get the identifier from the iterator
			UriBuilder root = uriInfo.getAbsolutePathBuilder();								// get the root URI
			String uri = root.clone().toTemplate();											// generate a string from the URI
			Vehicle v2 = new Vehicle();														// create a new empty container
			Vehicle target = service.getVehicle(identifier);								// get the stored vehicle from the service
			v2.setId(target.getId());
			v2.setOrigin(uri.replace(id.concat("/vehicles"),target.getOrigin()));
			v2.setPosition(uri.replace(id.concat("/vehicles"), target.getPosition()));
			v2.setDestination(uri.replace(id.concat("/vehicles"), target.getDestination()));
			v2.setEntryTime(target.getEntryTime());
			v2.setState(target.getState());
			v2.setCategory(target.getCategory());
			v2.setSelf(uri.replace("places/"+id+"/vehicles", "vehicles/"+target.getId()));
			v2.setPath(uri.replace("places/"+id+"/vehicles", "vehicles/"+target.getId()+"/path"));
			v2.setNewState(uri.replace("places/"+id+"/vehicles", "vehicles/"+target.getId()+"/state"));
			v2.setNewPosition(uri.replace("places/"+id+"/vehicles", "vehicles/"+target.getId()+"/position"));
			vehicles.getVehicle().add(v2);
		}
		return vehicles; // 200 OK
	}
	
	@GET
	@Path("/places/{id}/connectedTo")
    @ApiOperation(value = "getPlacesConnectedTo", notes = "read `Places`element representing next hops from a given place")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found, the place is not present in the system"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getPlacesConnectedTo(@PathParam("id") String id){
		Places places = service.getplacesConnectedTo(id);	// search the list of connected places to `id`
		if (places==null)									// check the return value
			throw new NotFoundException(); // if it is null, the resource does not exists
		UriBuilder root = uriInfo.getAbsolutePathBuilder();		// get the root URI
		String uri = root.clone().toTemplate();					// get the corresponding uri (string)
		Places places2 = new Places();							// new EMPTY places container (the final clone)
		for(Place p:places.getPlace()){																			// for each place
			Place temp = new Place();																			// create a new empty place container
			temp.setId(p.getId());																				// set the `id` field
			temp.setCapacity(p.getCapacity());																	// set the `capacity` field
			temp.setSelf(uri.replace(id.concat("/connectedTo"),p.getId()));										// set the `self` field
			temp.setConnectedTo(uri.replaceAll(id.concat("/connectedTo"),p.getId().concat("/connectedTo")));	// set the `connectedTo` field
			temp.setVehicles(uri.replaceAll(id.concat("/connectedTo"),p.getId().concat("/vehicles")));
			for(String identifier:p.getNextPlace())																// for each next place
				temp.getNextPlace().add(uri.replace(id.concat("/connectedTo"), identifier));					// set the `nextplace` link
			places2.getPlace().add(temp);																		// add the place to places2														
		}
		return places2; // 200 OK
	}
	
	@GET
	@Path("/vehicles")
    @ApiOperation(value = "getVehicles", notes = "read `Vehicles` object")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicles getVehicles(){
		Vehicles ret = new Vehicles();
		for(Vehicle target: service.getVehicles().getVehicle()){							// get all the vehicles from the service
			UriBuilder root = uriInfo.getAbsolutePathBuilder();								// get the root URI
			String uri = root.clone().toTemplate();											// generate a string from the URI
			Vehicle v2 = new Vehicle();														// create a new empty container
			v2.setId(target.getId());
			v2.setOrigin(uri.replace("vehicles", "places/".concat(target.getOrigin())));
			v2.setPosition(uri.replace("vehicles", "places/".concat(target.getPosition())));
			v2.setDestination(uri.replace("vehicles", "places/".concat(target.getDestination())));
			v2.setEntryTime(target.getEntryTime());
			v2.setState(target.getState());
			v2.setCategory(target.getCategory());
			v2.setSelf(uri.replace("vehicles","vehicles/".concat(target.getId())));
			v2.setPath(uri.replace("vehicles", "vehicles/".concat(target.getId()).concat("/path")));
			v2.setNewState(uri.replace("vehicles", "vehicles/".concat(target.getId()).concat("/state")));
			v2.setNewPosition(uri.replace("vehicles", "vehicles/".concat(target.getId()).concat("/position")));
			ret.getVehicle().add(v2);
		}
		return ret; // 200 OK
	}
	
	@GET
	@Path("/vehicles/{id}")
    @ApiOperation(value = "getVehicle", notes = "read `Vehicle` object")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found, the vehicle is not present in the system"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicle getVehicle(@PathParam("id") String id){
		Vehicle target = service.getVehicle(id);								// get the target vehicle from the service
		if(target == null)														// check the result
			throw new NotFoundException();	// if it is null, the resource does not exists
		UriBuilder root = uriInfo.getAbsolutePathBuilder();		// get the root URI
		String uri = root.clone().toTemplate();					// generate a string from the URI
		Vehicle v2 = new Vehicle();								// create a new empty container
		v2.setId(target.getId());
		v2.setOrigin(uri.replace("vehicles/".concat(target.getId()), "places/".concat(target.getOrigin())));
		v2.setPosition(uri.replace("vehicles/".concat(target.getId()), "places/".concat(target.getPosition())));
		v2.setDestination(uri.replace("vehicles/".concat(target.getId()), "places/".concat(target.getDestination())));
		v2.setEntryTime(target.getEntryTime());
		v2.setState(target.getState());
		v2.setCategory(target.getCategory());
		v2.setSelf(root.clone().toTemplate());
		v2.setPath(root.clone().path("path").toTemplate());
		v2.setNewState(root.clone().path("state").toTemplate());
		v2.setNewPosition(root.clone().path("position").toTemplate());
		return v2; // 200 OK											
	}
	
	@GET
	@Path("/vehicles/{id}/path")
    @ApiOperation(value = "getPath", notes = "read the vehicle `ShortestPath`")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found, the vehicle is not present in the system"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public ShortestPath getPath(@PathParam("id") String id){
		Vehicle v = service.getVehicle(id);										// search the vehicle in the db
		if(v == null)
			throw new NotFoundException();
		ConcurrentSkipListSet<String> res = service.getShortestPath(v.getId()); 	//check the shortest path
		if(res == null)
			throw new InternalServerErrorException();
		
		ShortestPath sp = new ShortestPath();									// create a new empty container
		UriBuilder root = uriInfo.getAbsolutePathBuilder();						// get the root URI
		String uri = root.clone().toTemplate();									// generate a string from the URI
		for(String s:res){														// for each place in `res`
			sp.getPlace().add(uri.replace("vehicles/".concat(id).concat("/path"), "palces/".concat(s)));	// create the corresponding uri
		}
		return sp; // 200 OK
	}

	// we can use PUT for creation (idempotent)
	/*
	 * 2. A vehicle that wants to enter the system has to request permission to the system. In the
	 * permission request the vehicle must specify the entrance (IN or INOUT) gate where it is,
	 * and the destination place it wants to go to, in addition to the information about the vehicle
	 * itself (plate id and vehicle type).
	 * The system may grant permission or not.
	 * 
	 * If the destination is not reachable, the system does not grant permission, but there may be 
	 * other reasons 
	 * (e.g. congestion or other system policies or vehicle already in the system) for not granting
	 * permission. 
	 * 
	 * If the permission is granted, the system computes a suggested path to the
	 * destination, adds the vehicle to the set of vehicles that are tracked in the system, with its
	 * current position set at the entrance gate and its initial state set to IN_TRANSIT, and
	 * communicates the suggested path to the vehicle.
	 * 
	 * If instead the permission is not granted, the
	 * system communicates the reason for not granting the access to
	 */
	@POST
	@Path("/vehicles")
    @ApiOperation(value = "createVehicle", notes = "create a new `Vehicle`")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 400, message = "Bad Request"),
    		@ApiResponse(code = 404, message = "Not found, the origin and/or destination are not present in the system"),
    		@ApiResponse(code = 409, message = "Conflict, the origin is not a gate or is not an IN or INOUT gate")
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public ShortestPath createVehicle(Vehicle vehicle){
		
		// --1-- check if the vehicle is already in the system
		Vehicle v = service.getVehicle(vehicle.getId());	// search the vehicle
		if( v != null)										// i already exists throw and exception
			throw new ConflictException();
		// --2-- check the gate type
		Place place = service.getPlace(vehicle.getOrigin());		// get the place from the db
		if( place == null)
			throw new NotFoundException(); // source not present in the system
		if(place.getGate() == null)
			throw new ConflictException(); // source is not a gate
		if( place.getGate()==Gate.OUT)
			throw new ConflictException(); // source is not a IN or INOUT gate
		// --3-- check if the destination is reachable and exists
		Place dest = service.getPlace(vehicle.getDestination());
		if(dest == null)
			throw new NotFoundException(); // destination not present in the system
		List<String> res = service.isReachable(vehicle.getOrigin(), vehicle.getDestination());
		if(res == null)
			throw new ConflictException(); // destination not reachable
		
		
		//return the shortest path
		ShortestPath path = new ShortestPath();					// create a new empty shortest path container
		UriBuilder root = uriInfo.getAbsolutePathBuilder();		// get the root URI
		for(String identifier: res){							// for each place in the `res` variable
			String ss = root.clone().toTemplate();
			path.getPlace().add(ss.replace("vehicles", "places/".concat(identifier)));
		}
		
		vehicle.setState(VState.IN_TRANSIT);					// update the vehicle state
		XMLGregorianCalendar xgc;								// try to generate the current date time
		try {
			xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(new DateTime().toGregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			throw new InternalServerErrorException();
		}
		vehicle.setCategory(vehicle.getCategory()); // not mandatory
		vehicle.setEntryTime(xgc);
		vehicle.setPosition(vehicle.getOrigin());
		vehicle.setPath(null);
		/*
		 * putIfAbsent: A value null will be returned if the key-value mapping is successfully 
		 * added to the hashmap object while if the id is already present on the hashmap the value 
		 * which is already existing will returned instead.
		 */
		if(service.storeShortestPath(vehicle.getId(),res)!=null)
			throw new InternalServerErrorException();
		if(service.createVehicle(vehicle)!=null)
			throw new InternalServerErrorException();
		
		// increment the number of vehicles in the place
		//service.incrementPlace(place.getId());
		return path; // 200 OK
		
	}
	
	/*
	 * 5. A tracked vehicle can request the system to change its state at any time. The request is
	 * always accepted by the system.
	 */
	@PUT
	@Path("/vehicles/{id}/state")
    @ApiOperation(value = "updateVehicle", notes = "update single vehicle"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No Content"),
    		@ApiResponse(code = 404, message = "Not Found, the vehicle is not present in the system"),
    		@ApiResponse(code = 400, message = "Bad Request"),
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public void updateVehicleState(@PathParam("id") String id,Vehicle vehicle){
		Vehicle vv = service.updateVehicleState(id,vehicle);
		if(vv == null)
			throw new NotFoundException();
		return; //204 No Content
	}
	
	/*
	 * 3. Whenever a tracked vehicle moves from a place to another one, it informs the system about
	 * the new place where it is, and the system records the new current position of the vehicle. 
	 * If the new place is not on the path suggested by the system for that vehicle, the system first
	 * checks if the new place is reachable from the previous current position of the vehicle.
	 * If so, it computes a new path from the new current position of the vehicle to the destination, and
	 * communicates this new path to the vehicle.
	 * If the path cannot be computed (e.g. because the destination is not reachable from the new current place),
	 * the vehicle remains without a suggested path.
	 * Finally, if the new place is not reachable from the previous current position
	 * of the vehicle, the request from the vehicle is considered wrong by the system, and nothing
	 * changes.
	 */
	
	@PUT
	@Path("/vehicles/{id}/position")
    @ApiOperation(value = "updateVehicle", notes = "update `Vehicle` position"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK, position updated, new shortest path available"),
    		@ApiResponse(code = 204, message = "No Contentn position updated,empty shortest path or currently on the previous shortest path"),
    		@ApiResponse(code = 404, message = "Not Found, the vehicle and/or position are not present in the system"),
    		@ApiResponse(code = 409, message = "Conflict, the new position is not reachable from the previous one"),
    		@ApiResponse(code = 400, message = "Bad Request"),
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public ShortestPath updateVehiclePosition(@PathParam("id") String id,Vehicle vehicle){
		// --1-- check if the vehicle is stored in the db
		Vehicle v = service.getVehicle(id);				// search the target vehicle
		if(v == null)									// check the return value
			throw new NotFoundException();
		
		// further improvements --2 -- check if the vehicle is in the PARKED state
		//	if(v.getState() == VState.PARKED)
		//		throw new BadRequestException();
		// --3-- check if the new place is a valid one
		
		if(vehicle.getPosition()==null)
			throw new BadRequestException();
		
		Place p = service.getPlace(vehicle.getPosition());
		if(p == null)
			throw new NotFoundException(); // position not present in the system
		
		
		// --4-- check if the new position is in the suggested path
		ConcurrentSkipListSet<String> path =service.getShortestPath(id);
		
		if(path.contains(p.getId())){
			// --4.1--
			// the vehicle is still on the suggested route
			
			// MY ADDITION
			//chech the reachability since you cannot go in the opposite direction
			// of a connection
			List<String> tmp = service.isReachable(v.getPosition(), p.getId());
			if(tmp == null)
				throw new ConflictException();
			
			// we can easily update the position with the new one
			Vehicle vv = service.setNewPosition(v.getId(),p.getId(),v.getPosition());
			if(vv == null)
				throw new InternalServerErrorException();
			return null; // position updated , return NO CONTENT

		}else{
			// --4.2--
			// the vehicle is not on the suggested route
			// check if the new place is reachable from the previous current position of the vehicle 
			List<String> tmp = service.isReachable(v.getPosition(), p.getId());
			if(tmp == null){ // if not
				// --4.2.1--
				// if the new place is not reachable from the previous current position
				// of the vehicle, the request from the vehicle is considered wrong by the system, and nothing
				// changes.
				throw new ConflictException();
			}else{// if yes
				// --4.2.2--
				//computes a new path from the new current position of the vehicle to the destination, and
				// communicates this new path to the vehicle
				List<String> newPath = service.isReachable(p.getId(), v.getDestination());
				if(newPath == null){
					//If the path cannot be computed (e.g. because the destination is not reachable from the new current place),
					//the vehicle remains without a suggested path.
					if(service.clearShortestPath(v.getId()) == null)
						throw new InternalServerErrorException();
					return null; // NO CONTENT
				}
				ShortestPath sp = new ShortestPath();									// create a new empty container
				UriBuilder root = uriInfo.getAbsolutePathBuilder();						// get the root URI
				String uri = root.clone().toTemplate();									// generate a string from the URI
				for(String s:newPath){														// for each place in `res`
					sp.getPlace().add(uri.replace("vehicles/".concat(id).concat("/position"), "palces/".concat(s)));	// create the corresponding uri
				}
				// update the position
				Vehicle vv = service.setNewPosition(v.getId(),p.getId(),v.getPosition());
				if(vv == null)
					throw new InternalServerErrorException();
				// update prev position counter
				service.decrementPlace(vehicle.getPosition());
				// update current position counter
				service.incrementPlace(vv.getPosition());
				return sp; //200 OK

			}
		}
		
		
		
	}	
	
	/*
	 * 7. At any time, an administrator can request to remove a vehicle from the set of vehicles that
	 * are tracked in the system and the request is always accepted by the system.
	 */
	@DELETE
	@Path("/vehicles/{id}")
    @ApiOperation(value = "deleteVehicle", notes = "delete single vehicle"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No content, vehicle deleated correctly"),
    		@ApiResponse(code = 404, message = "Not Found, the vehicle is not present in the system"),
    		})
	public Response deleteVehicle(@PathParam("id") String id){
		if (service.deleteVehicle(id) == null)
			throw new NotFoundException();
		return Response.status(Status.NO_CONTENT).build(); // no content
	}
	
	/*
	 * 4. When a tracked vehicle is in an OUT or INOUT gate, it can decide to exit from the system
	 * through that gate. When doing so, it contacts the system and communicates to the system
	 * that it has left the system. Upon receiving this information, the system removes the vehicle
	 * from the set of vehicles that are tracked in the system and forgets about it.
	 */
	@POST
	@Path("/vehicles/{id}/position")
    @ApiOperation(value = "exit", notes = "request to exit the system"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No content"),
    		@ApiResponse(code = 404, message = "Not Found, the vehicle and/or position are not present in the system"),
    		@ApiResponse(code = 409, message = "Conflict, position is not reachable or is not an OUT or INOUT gate"),
    		@ApiResponse(code = 400, message = "BadRequest"),
    		})
	public Response exit(@PathParam("id") String id,Vehicle tmp){
		// --1-- check if the vehicle exists
		Vehicle vehicle = service.getVehicle(id);
		if (vehicle == null)							
			throw new NotFoundException(); // vehicle not found
		// --2-- check if the gate allow to exit
		Place p = service.getPlace(tmp.getPosition());
		if(p == null)
			throw new NotFoundException(); // position is not present in the system
		
		List<String> list = service.isReachable(vehicle.getPosition(),p.getId());
		if(list == null)	// not reachable
			throw new ConflictException();
		if(p.getGate() == null)// it is not a gate
			throw new ConflictException();
		if(p.getGate() == Gate.IN)	// no OUT or INOUT
			throw new ConflictException();
		if (service.deleteVehicle(id) == null)
			Response.status(Status.INTERNAL_SERVER_ERROR).entity(" non funziona ").build();
		return null; // no content
		
	}
	
	
}
