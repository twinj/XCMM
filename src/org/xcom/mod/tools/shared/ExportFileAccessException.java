package org.xcom.mod.tools.shared;

import java.io.IOException;

public class ExportFileAccessException extends IOException {

	private static final long serialVersionUID = 1L;

	public ExportFileAccessException() {
	}

	public ExportFileAccessException(String msg) {
		super(msg);
	}	
}
