package it.polito.dp2.RNS.sol3.vehClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.ClientResponse.Status;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;
import it.polito.dp2.RNS.sol1.jaxb.Place;
import it.polito.dp2.RNS.sol1.jaxb.Places;
import it.polito.dp2.RNS.sol1.jaxb.RnsSystem;
import it.polito.dp2.RNS.sol1.jaxb.ShortestPath;
import it.polito.dp2.RNS.sol1.jaxb.VState;
import it.polito.dp2.RNS.sol1.jaxb.VType;
import it.polito.dp2.RNS.sol1.jaxb.Vehicle;

public class VehClient_ implements it.polito.dp2.RNS.lab3.VehClient {
	
	private URI baseUri;
	private Client client;
	private WebTarget target;
	private Map<String,URI> resources;
	private Map<String,String> place_link_map;
	private String plateID;
	
	public VehClient_(URI uri) {
		this.baseUri = uri;
		this.client = ClientBuilder.newClient();	    
		this.target = client.target(this.baseUri);
		this.resources = new HashMap<>();
		this.place_link_map = new HashMap<>();
		RnsSystem system = null;
		try {
			system = this.getRoot();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.resources.put("places", UriBuilder.fromUri(system.getPlaces()).build());
		this.resources.put("connections", UriBuilder.fromUri(system.getConnections()).build());
		this.resources.put("vehicles", UriBuilder.fromUri(system.getVehicles()).build());
		
		Places places = null;
		try {
			places = this.getPlaces();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// store the mapping link-identifier
		for(Place p : places.getPlace()){
			this.place_link_map.put(p.getSelf(), p.getId());
		}
		
	}
	/**
	 * Requests permission to the service to enter the system as a tracked vehicle
	 * If permission is granted by the system, returns the suggested path to the desired destination
	 * 
	 * @param plateId the plate id of the vehicle that is requesting permission
	 * @param inGate the id of the input gate for which the vehicle is requesting permission
	 * @param destination the destination place for which the vehicle is requesting permission
	 * @return the suggested path (the list of ids of the places of the suggested path, including source and destination)
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 * @throws UnknownPlaceException if the source or the destination is not a known place
	 * @throws WrongPlaceException if inGate is not the id of an IN or INOUT gate
	 * @throws EntranceRefusedException if permission to enter is not granted
	 */
	@Override
	public List<String> enter(String plateId, VehicleType type, String inGate, String destination)
			throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
		System.out.println("sto inserendo un veicolo");
		Vehicle v = new Vehicle();
		v.setId(plateId);
		v.setType(VType.valueOf(type.toString()));
		v.setOrigin(inGate);
		v.setDestination(destination);
		
		Response result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("vehicles"));
			result = target.request(MediaType.APPLICATION_XML).post(Entity.entity(v, MediaType.APPLICATION_XML));
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException();
		}
		
		// check error codes
		int code = result.getStatus();
		if(code == Status.NOT_FOUND.getStatusCode())
			throw new UnknownPlaceException();
		if(code == Status.CONFLICT.getStatusCode())
			throw new WrongPlaceException();
		if(code == Status.FORBIDDEN.getStatusCode())
			throw new EntranceRefusedException();
		
		ShortestPath path= result.readEntity(ShortestPath.class);
		// convert uri into identifier
		List<String> list = new ArrayList<>();
		for(String uri : path.getPlace()){
			list.add(this.place_link_map.get(uri));
		}
		this.plateID = plateId;
		return list;
	}

	/**
	 * Communicates to the service that the vehicle has changed its position to a new place
	 * Returns the new suggested path or null if the path has not changed
	 * 
	 * @param newPlace the id of the new place
	 * @return the new suggested path (the list of the ids of the places of the new suggested path) or null if the suggested path has not changed
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 * @throws UnknownPlaceException if newPlace is not the id of a known place
	 * @throws WrongPlaceException if newPlace is not the id of a place reachable from the previous position of the vehicle
	 */
	@Override
	public List<String> move(String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		Vehicle v = new Vehicle();
		v.setPosition(newPlace);
		
		Response result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("vehicles"));
			result = target.path(this.plateID).path("position").request(MediaType.APPLICATION_XML).post(Entity.entity(v, MediaType.APPLICATION_XML));
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException();
		}
		List<String> res = null;
		// check error codes
		int code = result.getStatus();
		if(code == Status.NOT_FOUND.getStatusCode())
			throw new UnknownPlaceException();
		if(code == Status.CONFLICT.getStatusCode())
			throw new WrongPlaceException();
		if(code == Status.OK.getStatusCode())
			res= result.readEntity(ShortestPath.class).getPlace();
		if(code == Status.NO_CONTENT.getStatusCode())
			res = null;
		return res;
		
	}

	/**
	 * Communicates to the service the new state of the vehicle
	 * 
	 * @param newState the new state of the vehicle
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	@Override
	public void changeState(VehicleState newState) throws ServiceException {
		Vehicle v = new Vehicle();
		v.setState(VState.valueOf(newState.toString()));
		
		Response result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("vehicles"));
			result = target.path(this.plateID).path("state").request(MediaType.APPLICATION_XML).put(Entity.entity(v, MediaType.APPLICATION_XML));
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException();
		}
		
		return;

	}

	/**
	 * Communicates to the service that the vehicle has exited the system
	 * 
	 * @param outGate the gate at which the vehicle has exited the system
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 * @throws UnknownPlaceException if outGate is not the id of a known place
	 * @throws WrongPlaceException if outGate is not the id of an OUT or INOUT gate or is not reachable from the previous position of the vehicle
	 */
	@Override
	public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		
		Vehicle v = new Vehicle();
		v.setPosition(outGate);

		Response result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("vehicles"));
			result = target.path(this.plateID).path("exit").request(MediaType.APPLICATION_XML).post(Entity.entity(v, MediaType.APPLICATION_XML));
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException();
		}

		// check error codes
		int code = result.getStatus();
		if(code == Status.CONFLICT.getStatusCode() && result.readEntity(String.class).matches("POSITION NOT PRESENT IN THE SYSTEM"))
			throw new UnknownPlaceException();
		if(code == Status.CONFLICT.getStatusCode() && result.readEntity(String.class).matches("POSITION NOT VALID"))
			throw new WrongPlaceException();
	
		return ;

	}
	
	/**
	 * Get root
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
	private Places getPlaces() throws Exception{
		Places result;
		try{
			WebTarget target = ClientBuilder.newClient().target(this.resources.get("places"));
			result = target.request(MediaType.APPLICATION_XML).get(Places.class);
				if(result == null)
					throw new Exception();
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return result;
	}
	

}
