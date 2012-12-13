/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xcom.main.shared.entities;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * @author Daniel Kemp
 */
@XmlRootElement(name="IniFile")
@XmlType(propOrder={"id", "iniFilename", "bytesSum", "searchHashLength", "searchHash", "changes", "isInstalled", "XMod"})
public class IniFile extends ModFile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private Integer id;
	
	private String iniFilename;

	private int searchHashLength;

	private int bytesSum;

	private String searchHash;


	private boolean isInstalled;
	
	private List<HexEdit> changes;
	
	
	private XMod xMod;

	// Clean constructor required for serialisation
	public IniFile() {
	}

	public IniFile(Integer id) {
		this.id = id;
	}

	public IniFile(Integer id, String filename, String searchHash,
			int hashLength, int sum) {
		this.id = id;
		this.iniFilename = filename;
		this.searchHash = searchHash;
		this.searchHashLength = hashLength;
		this.bytesSum = sum;
	}

	@XmlAttribute(name = "Id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlElement(name="IniFilename")
	public String getIniFilename() {
		return iniFilename;
	}

	public void setIniFilename(String filename) {
		this.iniFilename = filename;
	}

	@XmlElement(name="SearchHash")
	public String getSearchHash() {
		return searchHash;
	}

	public void setSearchHash(String searchHash) {
		this.searchHash = searchHash;
	}

	@XmlElement(name="SearchHashLength")
	public int getSearchHashLength() {
		return searchHashLength;
	}

	public void setSearchHashLength(int hashLength) {
		this.searchHashLength = hashLength;
	}

	@XmlElement(name="BytesSum")
	public int getBytesSum() {
		return bytesSum;
	}

	public void setBytesSum(int sum) {
		this.bytesSum = sum;
	}
	
	@XmlElement(name = "IsInstalled")
	public Boolean getIsInstalled() {
		return isInstalled;
	}

	public void setIsInstalled(Boolean b) {
		this.isInstalled = b;
	}

	@XmlElement(name="XMod")
	public XMod getXMod() {
		return xMod;
	}

	public void setXMod(XMod xMod) {
		this.xMod = xMod;
	}

	//@XmlTransient - if go to SOAP or REST may need to re think 
	@XmlElementWrapper(name="Changes")
	@XmlElement(name="HexEdit")
	public List<HexEdit> getChanges() {
		return changes;
	}

	public void setChanges(List<HexEdit> edits) {
		this.changes = edits;
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
		if (!(object instanceof IniFile)) {
			return false;
		}
		IniFile other = (IniFile) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.xcom.main.shred.entities.IniFile[ id=" + id + " ]";
	}

}
