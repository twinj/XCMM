package org.xcom.main.shared.entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BaseConfig {
	
	public final static String USER_DIR = System.getProperty("user.dir");
	public final static String PATH = USER_DIR + "\\config";	
	public final static String TOOLS_DIR = USER_DIR + "\\tools";
	public final static String MODS_DIR = USER_DIR + "\\mods";
	
	static Boolean configVerified = false;
	
	public static Path getDir() {
		Path dir = Paths.get(PATH).toAbsolutePath();

		if (configVerified == false) {
			if (Files.notExists(dir)) {
				try {
					Files.createDirectory(Paths.get(PATH).toAbsolutePath());
					configVerified = true;
				} catch (IOException e) {}
			}
		}
		return dir;
	}
	
	public static Path getModPath() {
		return Paths.get(MODS_DIR).toAbsolutePath();
	}
	
	public static Path getToolsPath() {
		return Paths.get(TOOLS_DIR).toAbsolutePath();
	}
	
}
