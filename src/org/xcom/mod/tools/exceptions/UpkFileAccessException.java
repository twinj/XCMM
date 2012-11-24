package org.xcom.mod.tools.exceptions;

import java.io.IOException;

public class UpkFileAccessException extends IOException {

	private static final long serialVersionUID = 1L;

	public UpkFileAccessException() {
	}

	public UpkFileAccessException(String msg) {
		super(msg);
	}	
}
