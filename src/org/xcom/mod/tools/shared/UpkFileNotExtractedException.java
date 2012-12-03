package org.xcom.mod.tools.shared;

import java.nio.file.Path;
import java.util.List;

public class UpkFileNotExtractedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	List<Path> unextractedFiles;

	public UpkFileNotExtractedException(List<Path> unextractedFiles) {
		this.unextractedFiles = unextractedFiles;
	}

	public UpkFileNotExtractedException(List<Path> unextractedFiles, String msg) {
		super(msg);
		this.unextractedFiles = unextractedFiles;
	}	
	
	public UpkFileNotExtractedException() {
	}
	
	public List<Path> getFiles() {
		return this.unextractedFiles;
	}
}
