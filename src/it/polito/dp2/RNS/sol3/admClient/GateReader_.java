package it.polito.dp2.RNS.sol3.admClient;

import java.util.Set;

import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.PlaceReader;

public class GateReader_ implements GateReader {
	
	// attributes
	public PlaceReader_ place;
	private GateType_ type;
	
	public GateReader_(PlaceReader_ place, GateType_ type) {
		if( place != null) this.place = place;
		if( type != null) this.type = type;
	}
	
	
	@Override
	public String getId() {
		return this.place.getId();
	}
	
	@Override
	public int getCapacity() {
		return this.place.getCapacity();
	}
	
	@Override
	public GateType getType() {
		return GateType.valueOf(this.type.toString());
	}

	@Override
	public Set<PlaceReader> getNextPlaces() {
		return this.place.getNextPlaces();
	}

}
