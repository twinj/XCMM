package org.xcom.main.shared.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.xcom.mod.gui.XCMGUI;

/** 
 * @author Daniel Kemp
 */
@XmlRootElement(name = "GameState")
@XmlType(propOrder = {
			"mods", "installData", "resources"
})
public class GameState extends BaseConfig implements Serializable, ModXml {
	
	private static final long serialVersionUID = -1857756335055766188L;

	public final static String PATH = "config\\state.xml";

	private List<XMod> mods = new Vector<XMod>();
	private List<ModInstall> installData = new Vector<ModInstall>();
	private List<ResFile> resources = new Vector<ResFile>();
	
	@XmlElementWrapper(name = "BaseMods")
	@XmlElement(name = "XMod")
	public List<XMod> getMods() {
		return mods;
	}
	
	public void setMods(List<XMod> mods) {
		this.mods = mods;
	}
	
	@XmlElementWrapper(name = "BaseModInstallData")
	@XmlElement(name = "ModInstall")
	public List<ModInstall> getInstallData() {
		return installData;
	}
	public void setInstallData(List<ModInstall> installData) {
		this.installData = installData;
	}
	
	@XmlElementWrapper(name = "Resources")
	@XmlElement(name = "ResFile")
	public List<ResFile> getResources() {
		return resources;
	}
	
	public void setResources(List<ResFile> resources) {
		this.resources = resources;
	}
	
	@Override
	public Path getBasePath() {
		return getDir();
	}
	
	@Override
	public Path getXmlSavePath() {
		return Paths.get(getDir().toString(), "state.xml");
	}
	
	@Override
	public String getPrintName() {
		return XCMGUI.GUI_NAME + ": GAME STATE";
	}
	

	public static Path getSavePath() {
		return Paths.get(getDir().toString(), "state.xml");
	}
}
