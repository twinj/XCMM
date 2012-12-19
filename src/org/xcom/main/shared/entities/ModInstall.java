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

import org.xcom.mod.tools.xshape.MHash;

@XmlRootElement(name = "ModInstall")
@XmlType(propOrder = {
			"id", "name", "hash", "XMod", "modVersion", "gameVersion", "resFiles"
})
public class ModInstall extends BaseMod implements Serializable, ModXml {
	
	private static final long serialVersionUID = -7133083554953556991L;
	
	public final static String FILE_EXT = ".xmod.install.log.xml";
	
	private Integer id;
	private String hash = MHash.TEMP;
	private String name;
	private String modVersion;
	private String gameVersion;
	private List<ResFile> resFiles;
	
	private String XMod;

	private boolean isInstalled = true;
	
	public ModInstall() {}
	
	public ModInstall(XMod mod, List<ResFile> resFiles) {
		this.name = mod.getName();
		this.hash = mod.getHash();
		this.modVersion = mod.getModVersion();
		this.gameVersion = mod.getGameVersion();
		this.XMod = mod.getXmlSavePath().toString();
		this.resFiles = resFiles;
	}
	
	@XmlElement(name = "ModVersion")
	public String getModVersion() {
		return modVersion;
	}
	
	public void setModVersion(String modVersion) {
		this.modVersion = modVersion;
	}
	
	@XmlElementWrapper(name = "ChangedFiles")
	@XmlElement(name = "ResFile")
	public List<ResFile> getResFiles() {
		return resFiles;
	}
	
	public void setResFiles(List<ResFile> resFiles) {
		this.resFiles = resFiles;
	}
	
	@XmlElement(name = "GameVersion")
	public String getGameVersion() {
		return gameVersion;
	}
	
	public void setGameVersion(String gameVersion) {
		this.gameVersion = gameVersion;
	}
	
	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name = "Hash")
	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	@XmlElement(name = "Id")
	public Integer getId() {
		return id;
	}
	
	@Override
	public Path getXmlSavePath() {
		return Paths.get("mods", name, name + FILE_EXT);
	}
	
	@Override
	public Path getBasePath() {
		return Paths.get("mods", name);
	}
	
	@Override
	public String getPrintName() {
		return "";
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlElement(name = "XModPath")
	public String getXMod() {
		return XMod;
	}
	
	public void setXMod(String path) {
		this.XMod = path;
	}
	
	public void setIsInstalled(boolean b) {
		this.isInstalled = b;
	}
	
	@XmlTransient
	public Boolean  getIsInstalled() {
		return isInstalled;
	}

	@XmlTransient
	public Path getXModPath() {
		return Paths.get(XMod);
	}
	
}
