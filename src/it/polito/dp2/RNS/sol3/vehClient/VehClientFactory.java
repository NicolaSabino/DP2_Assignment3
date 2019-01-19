package it.polito.dp2.RNS.sol3.vehClient;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.RNS.lab3.VehClient;
import it.polito.dp2.RNS.lab3.VehClientException;

public class VehClientFactory extends it.polito.dp2.RNS.lab3.VehClientFactory {

	@Override
	public VehClient newVehClient() throws VehClientException {
		URI uri = null;
		if(System.getProperty("it.polito.dp2.RNS.lab3.URL")==null)
			uri = UriBuilder.fromUri("http://localhost:8080/RnsSystem/rest").build();
		else
			uri = UriBuilder.fromUri(System.getProperty("it.polito.dp2.RNS.lab3.URL")).build();
		
		VehClient_ client = new VehClient_(uri);
		
		return client;
	}

}
