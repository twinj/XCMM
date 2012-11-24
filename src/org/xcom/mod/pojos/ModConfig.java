package org.xcom.mod.pojos;

import java.io.File;
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
@XmlType(propOrder = { "name", "author", "description", "originalFilePaths" })
public class ModConfig implements Serializable, ModXml {

	private static final long serialVersionUID = 1L;

	private final static String AUTHOR = "Author";
	private final static String NAME = "Name";
	private final static String DESCRIPTION = "Description";
	private final static String CHANGED_FILES = "ChangedFiles";
	private final static String FILE_EXT = ".xmod.config.xml";

	// xml properties
	protected String name;
	protected String author;
	protected String description;
	protected List<String> originalFilePaths;
	
	// Non xml
	private List<File> editedFiles;
	private List<File> originalFiles;

	public ModConfig() {
	}
	
	public ModConfig(String name, String author, String description,
			List<String> originalFilePaths) {
		super();
		this.name = name;
		this.author = author;
		this.description = description;
		this.originalFilePaths = originalFilePaths;
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

	@XmlElementWrapper(name = CHANGED_FILES)
	@XmlElement(name = "PathToOriginal")
	public List<String> getOriginalFilePaths() {
		return originalFilePaths;
	}

	public void setOriginalFilePaths(List<String> originalFilePaths) {
		this.originalFilePaths = originalFilePaths;
	}

	@XmlTransient
	public List<File> getEditedFiles() {
		return editedFiles;
	}

	public void setEditedFiles(List<File> editedFiles) {
		this.editedFiles = editedFiles;
	}

	@XmlTransient
	public List<File> getOriginalFiles() {
		return originalFiles;
	}

	public void setOriginalFiles(List<File> originalFiles) {
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