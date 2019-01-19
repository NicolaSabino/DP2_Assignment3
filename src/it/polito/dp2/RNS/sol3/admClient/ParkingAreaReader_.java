package it.polito.dp2.RNS.sol3.admClient;

import java.util.Set;

import it.polito.dp2.RNS.PlaceReader;

public class ParkingAreaReader_ implements it.polito.dp2.RNS.ParkingAreaReader {
	
	private PlaceReader_ place;
	private Set<String> services;
	
	public ParkingAreaReader_(PlaceReader_ place, Set<String> services) {
		if( place != null) this.place = place;
		if( services != null) this.services = services;
	}
	
	
	@Override
	public String getId() {
		return this.place.getId();
	}
	
	@Override
	public Set<String> getServices() {
		return this.services;
	}

	@Override
	public int getCapacity() {
		return this.place.getCapacity();
	}
	
	@Override
	public Set<PlaceReader> getNextPlaces() {
		return this.place.getNextPlaces();
	}

	

}
