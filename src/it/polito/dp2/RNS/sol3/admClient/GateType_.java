package it.polito.dp2.RNS.sol3.admClient;

import java.io.Serializable;

public enum GateType_ implements Serializable, Comparable<GateType_> {
	IN,
    OUT,
    INOUT;

    public String value() {
        return name();
    }

    public static GateType_ fromValue(String v) {
        return valueOf(v);
    }
}
