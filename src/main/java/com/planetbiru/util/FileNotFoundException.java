package com.planetbiru.util;

import java.io.IOException;

public class FileNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileNotFoundException(IOException ex) {
		super(ex);
	}

}
