package it.polito.dp2.RNS.sol3.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.eclipse.persistence.internal.oxm.Root;

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
    @ApiOperation(value = "getRnsSystem", notes = "reads main resource")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK")})
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
		return sys;
	}
	
	@GET
	@Path("/places")
    @ApiOperation(value = "getPlaces", notes = "searches places")
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
			}
			for(String identifier:p.getNextPlace())												// for each next place
				temp.getNextPlace().add(root.clone().path(identifier).toTemplate());			// set the `nextplace` link
			places2.getPlace().add(temp);														// add the place to places2														
		}
		return places2;
	}
	
	@GET
	@Path("/connections")
    @ApiOperation(value = "getConnections", notes = "searches connections")
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
		return connections;
	}
	
	@GET
	@Path("/places/gates")
    @ApiOperation(value = "getGates", notes = "searches gates")
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
			temp.setSelf(root.clone().path(p.getId()).toTemplate());							// set the `self` field
			temp.setConnectedTo(root.clone().path(p.getId()).path("connectedTo").toTemplate());	// set the `connectedTo` field
			temp.setVehicles(root.clone().path(p.getId()).path("vehicles").toTemplate());
			temp.setGate(p.getGate());															// set the `gate` fields
			for(String identifier:p.getNextPlace())												// for each next place
				temp.getNextPlace().add(root.clone().path(identifier).toTemplate());			// set the `nextplace` link
			places2.getPlace().add(temp);														// add the place to places2														
		}
		return places2;
	} 
	
	@GET 
	@Path("/places/roadSegments")
    @ApiOperation(value = "getRoadSegments", notes = "searches road segmetns")
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
			temp.setSelf(root.clone().path(p.getId()).toTemplate());							// set the `self` field
			temp.setConnectedTo(root.clone().path(p.getId()).path("connectedTo").toTemplate());	// set the `connectedTo` field
			temp.setVehicles(root.clone().path(p.getId()).path("vehicles").toTemplate());
			RoadSegment rs = new RoadSegment();													// create a new empty road segment
			rs.setName(p.getRoadSegment().getName());											// set `name`
			rs.setRoad(p.getRoadSegment().getRoad());											// set `road`
			temp.setRoadSegment(rs);															// attach rs to temp
			for(String identifier:p.getNextPlace())												// for each next place
				temp.getNextPlace().add(root.clone().path(identifier).toTemplate());			// set the `nextplace` link
			places2.getPlace().add(temp);														// add the place to places2														
		}
		return places2;
	}
	
	@GET
	@Path("/places/parkingAreas")
    @ApiOperation(value = "getParkingAreas", notes = "searches parking areas")
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
			temp.setSelf(root.clone().path(p.getId()).toTemplate());							// set the `self` field
			temp.setConnectedTo(root.clone().path(p.getId()).path("connectedTo").toTemplate());	// set the `connectedTo` field
			temp.setVehicles(root.clone().path(p.getId()).path("vehicles").toTemplate());
			ParkingArea pa = new ParkingArea();													// create a new empty parking area
			pa.getService().addAll(p.getParkingArea().getService());							// set `services`
			temp.setParkingArea(pa); 															// attach parking area to tmp
			for(String identifier:p.getNextPlace())												// for each next place
				temp.getNextPlace().add(root.clone().path(identifier).toTemplate());			// set the `nextplace` link
			places2.getPlace().add(temp);														// add the place to places2														
		}
		return places2;
	}
	
	@GET 
	@Path("/places/{id}")
    @ApiOperation(value = "getPlace", notes = "read single place"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public  Place getPlace(@PathParam("id") String id){
		Place target = service.getPlace(id);									// search the target place with the corresponding `id`
		if (target==null)														// check the return value
			throw new NotFoundException();										// if it is null, the resource does not exists
		
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
		return place;															// return the target place
	}
	
	@GET
	@Path("/places/{id}/connectedTo")
    @ApiOperation(value = "getPlacesConnectedTo", notes = "read places that are conected to the selected palce")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getPlacesConnectedTo(@PathParam("id") String id){
		Places places = service.getplacesConnectedTo(id);	// search the list of connected places to `id`
		if (places==null)									// check the return value
			throw new NotFoundException();					// if it is null, the resource does not exists
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
		return places2;
	}
	
	@GET // TODO
	@Path("/vehicles")
    @ApiOperation(value = "getVehicles", notes = "searches vehicles ")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicles getVehicles(){
		return service.getVehicles();							// get all the vehicles from the service
	}
	
	@GET // TODO
	@Path("/vehicles/{id}")
    @ApiOperation(value = "getVehicle", notes = "read single vehicle")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicle getVehicle(@PathParam("id") String id){
		Vehicle target = service.getVehicle(id);				// get the target vehicle from the service
		if(target == null)										// check the result
			throw new NotFoundException();						// if it is null, the resource does not exists
		return target;											// otherwise return the target vehicle
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
    @ApiOperation(value = "createVehicle", notes = "create a new vehicle")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 400, message = "Bad Request"),
    		@ApiResponse(code = 409, message = "Conflict")
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicle createVehicle(Vehicle vehicle){
		//UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    	//vehicle.setSelf(builder.clone().path(vehicle.toString()).toTemplate());
		//Vehicle v = service.createVehicle(vehicle); // creation of the item
		//if (v !=null)
		//	throw new ConflictException();	
		//return v;
		
		// --1-- check if the vehicle is already in the system
		Vehicle v = service.getVehicle(vehicle.getId());	// search the vehicle
		if( v != null)										// i already exists throw and exception
			throw new ConflictException("this vehicle already exists");
		// --2-- check the gate type
		Place place = service.getPlace(vehicle.getOrigin());		// get the place from the db
		if( place != null)
			throw new BadRequestException("the gate does not exists");
		if( place.getGate()==Gate.OUT)
			throw new BadRequestException("wrong gate type");
		// --3-- check if the destination is reachable
		//if(!service.isReachable(place.getId()))
			//throw new ConflictException();
		
		Vehicle ret = service.createVehicle(vehicle);		// create the vehicle
		return ret;
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
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		@ApiResponse(code = 400, message = "Bad Request"),
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Vehicle updateVehicleState(Vehicle vehicle){
		Vehicle v = service.updateVehicleState(vehicle);
		if(v == null)
			throw new NotFoundException();
		return v;
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
    @ApiOperation(value = "updateVehicle", notes = "update single vehicle"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		@ApiResponse(code = 400, message = "Bad Request"),
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places updateVehiclePosition(@PathParam("id") String id,Place place){
		Vehicle v = service.getVehicle(id);				// search the target vehicle
		if(v == null)									// check the return value
			throw new NotFoundException();				// if null the vehicle does not exists
		Places path = service.calculatePath(v,place);	// calculate the current/new path
		if(path == null)								// check the path status
			throw new ConflictException("The new place is not reachable from the previous current position"); // if is null throw a conflict
		return path;									// otherwise return the current/new path
	}	
	
	
	
	/*
	 * 4. When a tracked vehicle is in an OUT or INOUT gate, it can decide to exit from the system
	 * through that gate. When doing so, it contacts the system and communicates to the system
	 * that it has left the system. Upon receiving this information, the system removes the vehicle
	 * from the set of vehicles that are tracked in the system and forgets about it.
	 * 
	 * 7. At any time, an administrator can request to remove a vehicle from the set of vehicles that
	 * are tracked in the system and the request is always accepted by the system.
	 */
	@DELETE
	@Path("/vehicles/{id}")
    @ApiOperation(value = "deleteVehicle", notes = "delete single vehicle"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No content"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		//@ApiResponse(code = 409, message = "Conflict"), if a vehicle is not in a exit gate
    		})
	public void deleteVehicle(@PathParam("id") String id){
		boolean v = service.deleteVehicle(id);	// delete the vehicle `id`
		if (v== false)							// check the return value
			throw new NotFoundException();		// the vehicle does not exists
		return;
	}
	
}
