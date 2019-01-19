package it.polito.dp2.RNS.sol3.admClient;

import java.util.Calendar;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;


public class VehicleReader_  implements it.polito.dp2.RNS.VehicleReader {

	private IdentifiedEntityReader_ id;
	private Calendar entryTime;
	private PlaceReader_ origin;
	private PlaceReader_ position;
	private PlaceReader_ destination;
	private VehicleState_ state;
	private VehicleType_ type;
	
	/**
	 * Constructor
	 * @param id
	 * @param entryTime
	 * @param origin
	 * @param position
	 * @param destination
	 * @param state
	 * @param type
	 */
	public VehicleReader_(IdentifiedEntityReader_ id, Calendar entryTime, PlaceReader_ origin, 
			PlaceReader_ position, PlaceReader_ destination, VehicleState_ state, VehicleType_ type) {
		if(id != null) this.id = id;
		if(entryTime != null) this.entryTime = entryTime;
		if(origin != null) this.origin = origin;
		if(position != null) this.position = position;
		if(destination != null) this.destination = destination;
		if(state != null) this.state = state;
		if(type != null) this.type = type;
		
	}
	
	@Override
	public String getId() {
		return this.id.getId();
	}
	
	public PlaceReader getDestination() {
		return this.destination;
	}

	public Calendar getEntryTime() {
		return this.entryTime;
	}

	public PlaceReader getOrigin() {
		return this.origin;
	}

	public PlaceReader getPosition() {
		return this.position;
	}

	/**
	 * @Override
	 * return an object
	 * suitable respect the teacher's class
	 * `VehicleState`
	 */
	public VehicleState getState() {
		String tmp = this.state.toString();
		return VehicleState.valueOf(tmp);
	}

	/**
	 * @Override
	 * return an object
	 * suitable respect the teacher's class
	 * `VehicleType`
	 */
	public VehicleType getType() {
		String tmp = this.type.toString();
		return VehicleType.valueOf(tmp);
	}

}
