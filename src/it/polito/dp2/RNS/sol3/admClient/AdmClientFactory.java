package it.polito.dp2.RNS.sol3.admClient;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.RNS.lab3.AdmClient;
import it.polito.dp2.RNS.lab3.AdmClientException;

public class AdmClientFactory extends it.polito.dp2.RNS.lab3.AdmClientFactory {

	@Override
	public AdmClient newAdmClient() throws AdmClientException {
		URI uri = null;
		// Check if the `URL` system property is correctly setted
		if(System.getProperty("it.polito.dp2.RNS.lab3.URL")==null)
			uri = UriBuilder.fromUri("http://localhost:8080/RnsSystem/rest").build();
		else
			uri = UriBuilder.fromUri(System.getProperty("it.polito.dp2.RNS.lab3.URL")).build();
		
		AdmClient_ client = new AdmClient_(uri);
		//TODO throw
		
		return client;
	}

}
