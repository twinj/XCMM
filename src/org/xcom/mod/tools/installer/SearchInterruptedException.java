package org.xcom.mod.tools.installer;

public class SearchInterruptedException extends InterruptedException {

	private static final long serialVersionUID = 1L;

	public SearchInterruptedException() {
	}

	public SearchInterruptedException(String msg) {
		super(msg);
	}	
}
