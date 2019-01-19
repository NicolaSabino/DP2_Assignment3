package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.PlaceReader;

public class ConnectionReader_ implements it.polito.dp2.RNS.ConnectionReader {

	private PlaceReader from;
	private PlaceReader to;
	
	public ConnectionReader_(PlaceReader from, PlaceReader to) {
		if(from!= null) this.from = from;
		if(to != null) this.to = to;
	}
	
	@Override
	public PlaceReader getFrom() {
		return this.from;
	}

	@Override
	public PlaceReader getTo() {
		return this.to;
	}

}
