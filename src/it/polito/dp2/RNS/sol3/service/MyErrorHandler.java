package it.polito.dp2.RNS.sol3.service;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MyErrorHandler implements ErrorHandler {

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		System.err.println("Warning during json validation");
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		throw exception;

	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw exception;
	}

}
