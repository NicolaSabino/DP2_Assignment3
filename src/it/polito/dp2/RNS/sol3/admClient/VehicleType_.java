package it.polito.dp2.RNS.sol3.admClient;

import java.io.Serializable;

public enum VehicleType_ implements Serializable, Comparable<VehicleType_> {
	
	CAR,
    TRUCK,
    SHUTTLE,
    CARAVAN;

    public String value() {
        return name();
    }

    public static VehicleType_ fromValue(String v) {
        return valueOf(v);
    }
}
