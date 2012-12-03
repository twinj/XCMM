package org.xcom.main.shared.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "ModInstallLog")
@XmlType(propOrder = { "name", "author", "description", "installed",
		"originalFilePaths", "editedFilePaths" })
public class InstallLog extends ModConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	public final static String FILE_EXT = ".xmod.install.log.xml";
	public final static String EDITED_FILES = "EditedFiles";

	private final Date installed = new Date();

	public InstallLog() {
	}

	public InstallLog(String name, String author, String description,
			List<String> editedFilePaths, List<String> originalFilePaths) {
		super(name, author, description, originalFilePaths, editedFilePaths);
		this.originalFilePaths = originalFilePaths;

	}

	@Override
	public Path getXmlSavePath() {
		return Paths.get("mods", name, name + FILE_EXT);
	}

	@XmlElementWrapper(name = EDITED_FILES)
	@XmlElement(name = "PathToEdited")
	public List<String> getOriginalFilePaths() {
		return editedFilePaths;
	}

	public void setEditedFilePaths(List<String> editedFilePaths) {
		this.editedFilePaths = editedFilePaths;
	}

	@XmlElement(name = "InstallDate")
	@XmlSchemaType(name = "date")
	public Date getInstalled() {
		return installed;
	}

}
