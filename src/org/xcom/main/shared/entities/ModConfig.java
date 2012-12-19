package org.xcom.main.shared.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.xcom.mod.gui.XCMGUI;

/**
 * 
 * @author Daniel Kemp
 */
@XmlRootElement(name = "XModConfig")
@XmlType(propOrder = { "name", "author", "description", "version", "ini", "originalFilePaths", "editedFilePaths",  })
public class ModConfig implements Serializable, ModXml {

	private static final long serialVersionUID = 1L;

	private final static String AUTHOR = "Author";
	private final static String NAME = "Name";
	private final static String DESCRIPTION = "Description";
	private final static String ORIGINAL_FILES = "OriginalFiles";
	private final static String EDITED_FILES = "EditedFiles";
	private final static String FILE_EXT = ".xmod.config.xml";

	// xml properties
	protected String name;
	protected String author;
	protected String description;
	protected String ini;
	protected String version;
	protected List<String> originalFilePaths;
	protected List<String> editedFilePaths;

	
	// Non xml
	private List<Path> editedFiles;
	private List<Path> originalFiles;

	public ModConfig() {
	}
	
	public ModConfig(String name, String author, String description,
			List<String> originalFilePaths, List<String> editedFilePaths) {
		super();
		this.name = name;
		this.author = author;
		this.description = description;
		this.originalFilePaths = originalFilePaths;
		this.editedFilePaths = editedFilePaths;

	}

	@XmlElement(name = NAME)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = AUTHOR)
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@XmlElement(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlElement(name = "ModVersion")
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@XmlElement(name = "Ini")
	public String getIni() {
		return this.ini;
	}

	public void setIni(String ini) {
		this.ini = ini;
	}

	@XmlElementWrapper(name = ORIGINAL_FILES)
	@XmlElement(name = "Path")
	public List<String> getOriginalFilePaths() {
		return originalFilePaths;
	}

	public void setOriginalFilePaths(List<String> originalFilePaths) {
		this.originalFilePaths = originalFilePaths;
	}
	
	@XmlElementWrapper(name = EDITED_FILES)
	@XmlElement(name = "Path")
	public List<String> getEditedFilePaths() {
		return editedFilePaths;
	}

	public void setEditedFilePaths(List<String> editedFilePaths) {
		this.editedFilePaths = editedFilePaths;
	}

	@XmlTransient
	public List<Path> getEditedFiles() {
		return editedFiles;
	}

	public void setEditedFiles(List<Path> editedFiles) {
		this.editedFiles = editedFiles;
	}

	@XmlTransient
	public List<Path> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<Path> originalFiles) {
		this.originalFiles = originalFiles;
	}

	public Path getXmlSavePath() {
		return Paths.get("mods", name, "files", name + FILE_EXT);
	}
	
	public static Path getXmlSavePath(String name) {
		return Paths.get("mods", name, "files", name + FILE_EXT);
	}
	
	public Path getBasePath() {
		return Paths.get("mods", name);
	}
	
	public Path getEditedFilesPath() {
		return Paths.get("mods", name, "files", "edited");
	}
	
	public Path getOriginalFilesPath() {
		return Paths.get("mods", name, "files", "original");
	}
	
	@Override
	public String getPrintName() {
		return XCMGUI.GUI_NAME + ": MOD CONFIG";
	}
}