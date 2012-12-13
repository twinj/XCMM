package org.xcom.main.shared.entities;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

public class Install {
	private String name;
	private String author;
	private String description;
	private String ini;
	private Boolean isInstalled;
	private String modVersion;
	private String gameVersion;
	private List<XMod> xMods;

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
	
	@XmlElementWrapper(name = "XMods")
	@XmlElement(name = "XMod")
	public List<XMod> getResFiles() {
		return xMods;
	}
	
	public void setResFiles(List<XMod> xMods) {
		this.xMods = xMods;
	}
}
