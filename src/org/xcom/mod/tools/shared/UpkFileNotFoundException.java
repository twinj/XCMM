package org.xcom.mod.tools.shared;

import java.io.FileNotFoundException;

public class UpkFileNotFoundException extends FileNotFoundException {

	private static final long serialVersionUID = 1L;

	public UpkFileNotFoundException() {
	}

	public UpkFileNotFoundException(String msg) {
		super(msg);
	}	
}
