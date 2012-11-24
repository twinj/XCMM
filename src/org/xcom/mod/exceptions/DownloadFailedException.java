package org.xcom.mod.exceptions;

public class DownloadFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public DownloadFailedException() {
	}

	public DownloadFailedException(String msg) {
		super(msg);
	}	
}
