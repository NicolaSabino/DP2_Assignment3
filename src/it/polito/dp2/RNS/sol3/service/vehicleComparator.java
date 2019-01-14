package it.polito.dp2.RNS.sol3.service;

import java.util.Comparator;

import it.polito.dp2.RNS.sol1.jaxb.Vehicle;

public class vehicleComparator implements Comparator<Vehicle> {


	@Override
	public int compare(Vehicle v1, Vehicle v2) {
		return (v1.getId().compareTo(v2.getId()));
	}

}
