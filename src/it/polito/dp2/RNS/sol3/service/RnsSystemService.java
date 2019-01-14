package it.polito.dp2.RNS.sol3.service;

import java.util.List;

import it.polito.dp2.RNS.sol1.jaxb.Connection;
import it.polito.dp2.RNS.sol1.jaxb.Connections;
import it.polito.dp2.RNS.sol1.jaxb.Place;
import it.polito.dp2.RNS.sol1.jaxb.Places;
import it.polito.dp2.RNS.sol1.jaxb.ShortestPath;
import it.polito.dp2.RNS.sol1.jaxb.Vehicle;
import it.polito.dp2.RNS.sol1.jaxb.Vehicles;

public class RnsSystemService {
	
	private RnsSystemDB db = RnsSystemDB.getDB(); // get the SINGLETON instance of the DB

	public Places getPlaces(SearchScope scope) {
		Places places = new Places();				// create an empty container of places
		List<Place> list = places.getPlace();		// get the reference of the list
		list.addAll(db.getPlaces(scope));			// add all the elements from the database
		return places;
	}

	public Place getPlace(String id) {
		return db.getPlace(id);
	}


	public Places getplacesConnectedTo(String id) {
		Places places = db.getPlacesConnectedTo(id);		// get the next places from `db`
		if(places == null)									// check the return value
			return null;									// if null `id` doesn't exist
		return places;										// otherwise return the whole list
	}

	public Vehicles getVehicles() {
		Vehicles vehicles = new Vehicles();				// create an empty container of places
		List<Vehicle> list = vehicles.getVehicle();		// get the reference of the list
		list.addAll(db.getVehicles());	// add all the elements from the database
		return vehicles;
	}

	public Vehicle getVehicle(String id) {
		return db.getVehicle(id);
	}


	public Vehicle createVehicle(Vehicle vehicle) {
		return db.createVehicle(vehicle);
	}

	public Vehicle updateVehicleState(Vehicle vehicle) {
		return db.updateVehicleState(vehicle);
	}

	public Vehicle updateVehiclePosition(Vehicle vehicle) {
		return db.updateVehiclePosition(vehicle);
	}

	public Places calculatePath(Vehicle vehicle, Place place) {
		List<Place> list = db.calculatePath(vehicle,place);	// get the path from `db`
		if(list == null)									// check the return value
			return null;									// if null `vehicle` doesn't exist
		Places places = new Places();						// create a new empty container
		places.getPlace().addAll(list);						// add the whole list
		return places;
	}

	public boolean deleteVehicle(String id) {
		return db.deleteVehicle(id);
	}

	public Connections getConnections() {
		Connections connections = new Connections();				// create an empty container of places
		List<Connection> list = connections.getConnection();		// get the reference of the list
		list.addAll(db.getConnections());							// add all the elements from the database
		return connections;
	}

	public List<String> isReachable(String from, String to) {
		return db.isReachable(from,to);
	}

	public ShortestPath storeShortestPath(String id,ShortestPath path) {
		return db.storeShortesPath(id, path);
	}

}
