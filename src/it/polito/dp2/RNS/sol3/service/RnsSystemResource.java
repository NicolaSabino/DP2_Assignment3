package it.polito.dp2.RNS.sol3.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol1.jaxb.Place;
import it.polito.dp2.RNS.sol1.jaxb.PlaceType;
import it.polito.dp2.RNS.sol1.jaxb.Places;
import it.polito.dp2.RNS.sol1.jaxb.RnsSystem;
import io.swagger.annotations.ApiResponse;

@Path("/RnsSystem") // main path
@Api(value = "/RnsSystem")
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
		RnsSystem sys = new RnsSystem();										// create an empty container
		UriBuilder root = uriInfo.getAbsolutePathBuilder();						// get the absolute path of the request.
		sys.setSelf(root.toTemplate());											// set the `self` field
		sys.setGates(root.clone().path("gates").toTemplate());					// set the `gates` field
		sys.setRoadSegments(root.clone().path("roadSegments").toTemplate());	// set the `RoadSegments" field
		sys.setParkingAreas(root.clone().path("parkingAreas").toTemplate());	// set the `parlingAreas" field
		sys.setConnections(root.clone().path("connections").toTemplate());		// set the `connections" field
		sys.setVehicles(root.clone().path("vehicles").toTemplate());			// set the `vehicles" field
		return sys;
	}
	
	@GET
	@Path("/places")
    @ApiOperation(value = "getPlaces", notes = "searches places")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getPlaces(@QueryParam("type") String keyword){
		return service.getPlaces(SearchScope.ALL, keyword);	// get all the places from the service
	}
	
	@GET
	@Path("/places/gates")
    @ApiOperation(value = "getGates", notes = "searches gates")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getGates(@QueryParam("type") String keyword){
		return service.getPlaces(SearchScope.GATES, keyword); // get all the gates from the service
	} 
	
	@GET
	@Path("/places/roadSegments")
    @ApiOperation(value = "getRoadSegments", notes = "searches road segmetns")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getRoadSegments(@QueryParam("type") String keyword){
		return service.getPlaces(SearchScope.ROADSEGMETNS, keyword); // get all the road segments from the service
	}
	
	@GET
	@Path("/places/parkingAreas")
    @ApiOperation(value = "getParkingAreas", notes = "searches parking areas")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getParkingAreas(@QueryParam("type") String keyword){
		return service.getPlaces(SearchScope.PARKINGAREAS, keyword); // get all the parking areas from the service
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
	public  Place getPlace(@PathParam("id") long id){
		Place target = service.getPlace(id);		// search the target place with the corresponding `id`
		if (target==null)							// check the return value
			throw new NotFoundException();			// if is it null, the resource does not exists
		return target;								// otherwise return the target place
	}
	
	@GET
	@Path("/palces/{id}/connectedTo")
    @ApiOperation(value = "getPlaceConnectedTo", notes = "read places that are conected to the selected palce")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Places getPlaceConnectedTo(@PathParam("id") long id){
		Place target = service.getPlace(id);		// search the target place with the corresponding `id`
		
		return null;
	} // TODO  we obtain a list of places
	
	@GET
	@Path("/vehicles")
    @ApiOperation(value = "getVehicles", notes = "searches vehicles ")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK")})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public void getVehicles(){} // TODO  we obtain a list of places
	
	@GET
	@Path("/vehicles/{id}")
    @ApiOperation(value = "getVehicle", notes = "read single vehicle")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "OK"),
    		@ApiResponse(code = 404, message = "Not Found"),
    		})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public void getVehicle(){} // TODO  we obtain a list of places

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
	public Response createVehicle(){return null;} // TODO
	
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
	 * 
	 * 5. A tracked vehicle can request the system to change its state at any time. The request is
	 * always accepted by the system.
	 */
	@PUT
	@Path("/vehicles/{id}")
    @ApiOperation(value = "updateVehicle", notes = "update single vehicle"
	)
    @ApiResponses(value = {
    		@ApiResponse(code = 204, message = "No Content"),	// new position stored
    		@ApiResponse(code = 200, message = "OK"),			// new path calculated
    		@ApiResponse(code = 404, message = "Not Found"),
    		@ApiResponse(code = 400, message = "Bad Request"),
    		})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public void updateVehicle(){} // TODO
	
	
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
	public void deleteVehicle(){}
	
}
