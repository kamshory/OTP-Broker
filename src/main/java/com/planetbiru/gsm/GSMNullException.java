package com.planetbiru.gsm;

public class GSMNullException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errorCode = "";
	/**
	 * Default constructor
	 */
	public GSMNullException() 
	{ 
		super(); 
	}
	/**
	 * Constructor with the message
	 * @param message Message
	 */
	public GSMNullException(String message) 
	{ 
		super(message); 
	}
	/**
	 * Constructor with the message
	 * @param message Message
	 */
	public GSMNullException(String message, String errorCode) 
	{ 
		super(message); 
		this.errorCode = errorCode;
	}
	/**
	 * Constructor with the message and cause
	 * @param message Message
	 * @param cause Cause
	 */
	public GSMNullException(String message, Throwable cause) 
	{ 
		super(message, cause); 
	}
	/**
	 * Constructor with cause
	 * @param cause Cause
	 */
	public GSMNullException(Throwable cause) 
	{ 
		super(cause); 
	}
	public String getErrorCode() {
		return errorCode;
	}
}
