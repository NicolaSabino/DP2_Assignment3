package it.polito.dp2.RNS.sol3.service;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/** 
 * Thrown to return a 409 Conflict 
 * response with optional Location header and entity. 
 */
public class ConflictException extends WebApplicationException {

	private static final long serialVersionUID = 1L;

	// empty exception
	public ConflictException() {
		super(Response.Status.CONFLICT);
	}
	// exception with custom messages
	public ConflictException(String message) {
		super(message, Response.Status.CONFLICT);
	}
}
