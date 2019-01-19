package it.polito.dp2.RNS.sol3.admClient;

import java.net.URI;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;


/**
* AdmClient is an interface for interacting with the RNS service as administrator.
* AdmClient extends RnsReader: an implementation of AdmClient is expected to contact the RNS service
* at initialization time, and get from the service the information about places and their connections.
* This information is stored in the implementation object and remains fixed for the lifetime of the object.
* For what concerns vehicles, an implementation of AdmClient always returns fresh information through
* getUpdatedVehicles or getUpdatedVehicle. Whenever one of these methods is called, the implementation
* contacts the service and returns the fresh information just obtained from the service.
* Instead, when the getVehicles or getVehicle inherited from RnsReader are called, an implementation of
* AdmClient does not return any information about vehicles (i.e. it returns, respectively, an empty set or null).
*/
public class AdmClient_ implements it.polito.dp2.RNS.lab3.AdmClient {
	
	private URI baseUri;
	private Client client;
	private WebTarget target;
	private UtilityMap utility;
	
	public AdmClient_(URI uri){
		this.baseUri = uri;
		client = ClientBuilder.newClient();	    
		target = client.target(this.baseUri);
		this.utility = new UtilityMap(this.target);	// it will load information from the server
	}


	/**
	 * Gets readers for all the connections available in the RNS system.
	 * @return a set of interfaces for reading all available connections.
	 */
	@Override
	public Set<ConnectionReader> getConnections() {
		Set<it.polito.dp2.RNS.ConnectionReader> result_set = new HashSet<>(this.utility.c_list);
		return result_set;
	}

	/**
	 * Gets readers for all the gates available in the RNS system with the given type
	 * @param type - the required gate type or null to get readers for all gates
	 * @return a set of interfaces for reading the selected gates
	 */
	@Override
	public Set<GateReader> getGates(GateType arg0) {
		Set<it.polito.dp2.RNS.GateReader> result_set = new HashSet<>();
		if(arg0 != null){	// select all the gates from a given type specified in arg0
			String type = arg0.toString();
			// in order to iterate trough a map 
			// we get the associated entry set
			for(Map.Entry<String,GateReader_> entity: this.utility.g_map.entrySet()){ 		// for each gate in the system
				if(entity.getValue().getType().toString().compareTo(type) == 0){			// if the type is the selected one by arg0
					result_set.add(entity.getValue()); 										// add the gate reader to the return_set
				}
			}
		}else{	// otherwise select all the gates
			result_set.addAll(this.utility.g_map.values());
		}
		return result_set;
	}

	/**
	 * Gets readers for all the parking areas available in the RNS system having the specified services
	 * @param services - the set of services, or null to get all parking areas
	 * @return a set of interfaces for reading the selected parking areas
	 */
	@Override
	public Set<ParkingAreaReader> getParkingAreas(Set<String> arg0) {
		Set<it.polito.dp2.RNS.ParkingAreaReader> result_set = new HashSet<>();
		if(arg0 != null){ // prune the set of parking areas using the set of Services
			// in order to iterate trough a map 
			// we get the associated entry set
			for(Map.Entry<String, ParkingAreaReader_> entry: this.utility.pa_map.entrySet()){ // for each parking area in the system
				Set<String> services = entry.getValue().getServices(); 	// get the related services for a given parking area
				if(this.utility.isSubset(services, arg0)){				// if is a subset
					result_set.add(entry.getValue());					// add it to the result set
				}
			}
		}else{ // otherwise add all parking area in the system
			result_set.addAll(this.utility.pa_map.values());
		}
		return result_set;
	}

	/**
	 * Gets a reader for a single place, available in the RNS system, given its id.
	 * @param id - the unique id of the place to get.
	 * @return an interface for reading the place with the given id or null if a place with the given id is not available in the system.
	 */
	@Override
	public PlaceReader getPlace(String arg0) {
		PlaceReader_ p = this.utility.p_map.get(arg0);
		return p;
	}

	/**
	 * Gets readers for all the places available in the RNS system whose ids have the specified prefix.
	 * @param idPrefix - the id prefix for selecting places or null to get places with all ids.
	 * @return a set of interfaces for reading the selected places.
	 */
	@Override
	public Set<PlaceReader> getPlaces(String arg0) {
		Set<it.polito.dp2.RNS.PlaceReader> result_set = new HashSet<>();
		if(arg0 != null){ // we have to select only places with a given prefix
			Map<String,PlaceReader_> map = this.utility.getByPrefix(arg0);	// return a pruned map of places
			result_set.addAll(map.values());								// add the pruned map in the result set
		}else{	// otherwise add all the places stored in p_map in result set
			result_set.addAll(this.utility.p_map.values());
		}
		return result_set;
	}

	/**
	 * Gets readers for all the road segments available in the RNS system belonging to the road with the given name
	 * @param roadName - the name of the road, or null to get readers for all the road segments of all roads
	 * @return a set of interfaces for reading the selected road segments
	 */
	@Override
	public Set<RoadSegmentReader> getRoadSegments(String arg0) {
		Set<it.polito.dp2.RNS.RoadSegmentReader> result_set = new HashSet<>();
		if(arg0 != null){ // select all the road segments belonging to a specific road indicated via arg0
			// in order to iterate trough a map 
			// we get the associated entry set
			for(Map.Entry<String,RoadSegmentReader_> entry: this.utility.rs_map.entrySet()){
				if(entry.getValue().getRoadName().compareTo(arg0) == 0){// check if the road name is the indicate one
					result_set.add(entry.getValue()); // if so, add it to the result set
				}
			}
		}else{ // otherwise select all the road segments in the system
			result_set.addAll(this.utility.rs_map.values());
		}
		return result_set;
	}

	/**
	 * Gets a reader for a single vehicle, available in the RNS system, given its plate id.
	 * @param id - the plate id of the vehicle to get.
	 * @return an interface for reading the vehicle with the given plate id or null if a vehicle with the given plate id is not available in the system.
	 */
	@Override
	public VehicleReader getVehicle(String arg0) {
		VehicleReader_ v = this.utility.v_map.get(arg0);
		return v;
	}

	/**
	 * Gets readers for a selection of all the vehicles that are in the RNS system. The selection can be made according to a number of parameters:
	 * - the entrance date and time since when vehicles have to be selected 
	 * - the types of vehicles to be selected 
	 * - the states of vehicles to be selected
	 * @param since - the entrance date/time since when vehicles have to be selected or null to select vehicles with any entrance date/time.
     * @param types - the set of types of vehicles that have to be selected or null to select vehicles of any type.
     * @param state - the state of vehicles to be selected or null to select vehicles in any state.
     * @return a set of interfaces for reading the selected vehicles.
	 */
	@Override
	public Set<VehicleReader> getVehicles(Calendar arg0, Set<VehicleType> arg1, VehicleState arg2) {
		Set<it.polito.dp2.RNS.VehicleReader> result_set = new HashSet<>();
		if(arg0 != null){ // select all vehicles according to date and time of entrance
			for(Map.Entry<String, VehicleReader_> entry: this.utility.v_map.entrySet()){ // for each vehicle
				if(entry.getValue().getEntryTime().compareTo(arg0) >= 0){					// if  the entrance is after (or the same) the indicated date and time
					result_set.add(entry.getValue());										// add the vehicle to the result set
				}
			}
		}
		if(arg1 != null){ // select all vehicles according the indicated type
			for(Map.Entry<String, VehicleReader_> entry: this.utility.v_map.entrySet()){ // for each vehicle
				if(entry.getValue().getType().toString().compareTo(arg1.toString()) == 0){	// if the type is the specified one
					result_set.add(entry.getValue());										// add the vehicle to the result set
				}
			}
		}
		if(arg2 != null){ // select all vehicles according the indicated state
			for(Map.Entry<String, VehicleReader_> entry: this.utility.v_map.entrySet()){ // for each vehicle
				if(entry.getValue().getState().toString().compareTo(arg2.toString()) == 0){	// if the type is the specified one
					result_set.add(entry.getValue());										// add the vehicle to the result set
				}
			}
		}
		if(arg0 == null && arg1==null && arg2==null){ // select all vehicles
			result_set.addAll(this.utility.v_map.values());
		}
		return result_set;
	}

	/**
	 * Gets readers for all the vehicles that are currently in the place with the given id, in the RNS system.
	 * If no place is specified, return the readers of all the vehicles that are currently in the system
	 * @param place the place for which we want to get vehicle readers or null to get readers for all places.
	 * @return a set of interfaces for reading the selected vehicles.
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	@Override
	public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException {
		if(place == null){
			try {
				return this.utility.getVehicles();
			} catch (Exception e) {
				e.printStackTrace();
				throw new ServiceException();
				
			}
		}else{
			try {
				return this.utility.getVehicles(place);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ServiceException();
			}
		}
	}

	/**
	 * Gets a reader for a single vehicle, available in the RNS system, given its plate id.
	 * The returned information is obtained from the remote service when the method is called.
	 * @param id the plate id of the vehicle to get.
	 * @return an interface for reading the vehicle with the given plate id or null if a vehicle with the given plate id is not available in the system.
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	@Override
	public VehicleReader getUpdatedVehicle(String id) throws ServiceException {
		
		VehicleReader vehicle = null;
		try {
			vehicle = this.utility.getVehicle(id);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException();
		}
		return vehicle;
	}

}
