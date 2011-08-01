package com.mlopez.service;

public class DeportesServiceException extends Exception{

	private static final long serialVersionUID = 5686547313305097610L;

	public DeportesServiceException(String message, Throwable e) {
		super (message, e);
	}
	
	public DeportesServiceException(String message) {
		super (message);
	}
	
}
