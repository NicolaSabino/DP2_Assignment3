package it.polito.dp2.RNS.sol3.service;

// SINGLETON  CLASS FILE
public class RnsSystemDB {

	private static RnsSystemDB db = new RnsSystemDB();
	
	/**
	 * Private constructor of the singleton
	 */
	private RnsSystemDB(){
		
	}
	
	/**
	 * It allows to access externally to the singleton 
	 * @return db
	 */
	public static RnsSystemDB getDb() {
		return db;
	}
}
