package org.xcom.main.shared.entities;

import java.util.List;

import org.xcom.mod.tools.xshape.MHash;

public abstract class BaseMod extends ModFile {
	protected Integer id;
	protected String hash = MHash.TEMP;
	protected String name;
	protected String modVersion;
	protected String gameVersion;
	protected List<ResFile> resFiles;
	
	public abstract String getModVersion();
	public abstract void setModVersion(String modVersion);
	public abstract List<ResFile> getResFiles();
	public abstract void setResFiles(List<ResFile> resFiles);	
	public abstract String getGameVersion();	
	public abstract void setGameVersion(String gameVersion);
	public abstract String getName();	
	public abstract void setName(String name);
	public abstract String getHash();
	public abstract void setHash(String hash);
	public abstract Integer getId();
	public abstract void setId(Integer id);

}
