package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.IdentifiedEntityReader;

public class IdentifiedEntityReader_ implements IdentifiedEntityReader {
	
	protected String id;
	/**
	 * Constructor
	 * @param id
	 */
	public IdentifiedEntityReader_(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return this.id;
	}

}
