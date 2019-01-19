package it.polito.dp2.RNS.sol3.admClient;

import java.util.Set;

import it.polito.dp2.RNS.PlaceReader;

public class RoadSegmentReader_ implements it.polito.dp2.RNS.RoadSegmentReader {

	private PlaceReader_ place;
	private String name;
	private String roadName;
	
	

	public RoadSegmentReader_(PlaceReader_ place, String name, String roadName ) {
		if(place != null) this.place = place;
		if(name != null) this.name = name;
		if(roadName != null) this.roadName = roadName;
	}
	
	@Override
	public String getId() {
		return this.place.getId();
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getRoadName() {
		return this.roadName;
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
