package org.xcom.mod.tools.exceptions;

import java.nio.file.Path;
import java.util.List;

public class UpkFileNotDecompressedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	List<Path> uncompedFiles;

	public UpkFileNotDecompressedException(List<Path> uncompedFiles) {
		this.uncompedFiles = uncompedFiles;
	}

	public UpkFileNotDecompressedException(List<Path> uncompedFiles, String msg) {
		super(msg);
		this.uncompedFiles = uncompedFiles;
	}	
	
	public UpkFileNotDecompressedException() {
	}
	
	public List<Path> getFiles() {
		return this.uncompedFiles;
	}
}
