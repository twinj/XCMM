/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xcom.main.shared.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.xcom.mod.gui.XCMGUI;
import org.xcom.mod.tools.xshape.MHash;

/**
 * 
 * @author Daniel Kemp
 */
@XmlRootElement(name = "XComMod")
@XmlType(propOrder = {
			"id", "name", "author", "modVersion", "description", "ini", "hash", "gameVersion",
			"resFiles", "iniFiles"
})
public class XMod extends ModFile implements Serializable, ModXml {
	
	private static final long serialVersionUID = 1L;
	
	private static final String EXPORT_FILE_EXT = ".xmod.export.xml";
	private static final String EXPORT_FILES_ORIGINAL = "files\\original\\";
	private static final String EXPORT_FILES_EDITED = "files\\edited\\";
	
	private Integer id;
	private String hash = MHash.TEMP;	
	private String name;	
	private String author;
	private String description;
	private String modVersion;
	private String gameVersion;
	private String ini;
	private boolean isInstalled;
	private List<ResFile> resFiles;
	private List<IniFile> iniFiles;
	
	// Clean constructor required for serialisation
	public XMod() {}
	
	public XMod(Integer id) {
		this.id = id;
	}
	
	public XMod(Integer id, String hash, String name, String author) {
		this.id = id;
		this.hash = hash;
		this.name = name;
		this.author = author;
	}
	
	@XmlAttribute(name = "Id")
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlElement(name = "Hash")
	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name = "Author")
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	@XmlElement(name = "Description")
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlElement(name = "Ini")
	public String getIni() {
		return ini;
	}
	
	public void setIni(String ini) {
		this.ini = ini;
	}
	
	// @XmlElement(name = "IsInstalled")
	@XmlTransient
	public Boolean getIsInstalled() {
		return isInstalled;
	}
	
	public void setIsInstalled(Boolean b) {
		this.isInstalled = b;
	}
	
	@XmlElement(name = "ModVersion")
	public String getModVersion() {
		return modVersion;
	}
	
	public void setVersion(String version) {
		this.modVersion = version;
	}
	
	@XmlElement(name = "Version")
	public String getGameVersion() {
		return gameVersion;
	}
	
	public void setGameVersion(String version) {
		this.gameVersion = version;
	}
	
	@XmlElementWrapper(name = "ChangedFiles")
	@XmlElement(name = "ResFile")
	public List<ResFile> getResFiles() {
		return resFiles;
	}
	
	public void setResFiles(List<ResFile> files) {
		this.resFiles = files;
	}
	
	@XmlElementWrapper(name = "ChangedFiles")
	@XmlElement(name = "IniFile")
	public List<IniFile> getIniFiles() {
		return iniFiles;
	}
	
	public void setIniFiles(List<IniFile> files) {
		this.iniFiles = files;
	}
	
	@XmlTransient
	public Path getXmlSavePath() {
		return Paths.get("mods", name, name + EXPORT_FILE_EXT);
	}
	
	public static Path getExportPath(String name) {
		return Paths.get("mods", name, name + EXPORT_FILE_EXT);
	}
	
	@XmlTransient
	public Path getBasePath() {
		return Paths.get("mods", name);
	}
	
	@XmlTransient
	public Path getOriginalFilesSavePath() {
		return Paths.get("mods", name, EXPORT_FILES_ORIGINAL);
	}
	
	@XmlTransient
	public Path getEditedFilesSavePath() {
		return Paths.get("mods", name, EXPORT_FILES_EDITED);
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}
	
	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are
		// not set
		if (!(object instanceof XMod)) {
			return false;
		}
		XMod other = (XMod) object;
		if ((this.id == null && other.id != null)
					|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "org.xcom.main.shared.entities.XMod[ id=" + id + " ]";
	}
	
	@Override
	public String getPrintName() {
		return XCMGUI.GUI_NAME + ": MOD EXPORT";
	}
	
}
