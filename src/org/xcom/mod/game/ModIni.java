package org.xcom.mod.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.xcom.mod.Main;

public class ModIni {
	
	private static final int READ_AHEAD_LIMIT = 1024; // This is a minimum for
																										// current file may need to
																										// increase
	
	private static final String COMMENTS = ";#";
	private static final String OPERATORS = ":=";
	static final char SECTION_BEGIN = '[';
	static final char SECTION_END = ']';
	
	private Path path;
	
	public ModIni(Path path) {
		this.path = path;
		mapIni(path);
	}
	
	private void mapIni(Path path) {
		
		try (BufferedReader is = Files.newBufferedReader(path, Main.DEFAULT_FILE_ENCODING)) {
			
			is.mark(READ_AHEAD_LIMIT);
			for (String line = is.readLine(); line != null; line = is.readLine()) {
				
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
}
