/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xcom.mod.pojos;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * @author Daniel Kemp
 */
@Entity
@Table(name = "inifile", catalog = "xmodstore", schema = "")
@XmlRootElement(name="IniFile")
@XmlType(propOrder={"id", "iniFilename", "bytesSum", "searchHashLength", "searchHash", "changes", "isInstalled", "XMod"})
@NamedQueries({
		@NamedQuery(name = "IniFile.findAll", query = "SELECT i FROM IniFile i"),
		@NamedQuery(name = "IniFile.findById", query = "SELECT i FROM IniFile i WHERE i.id = :id"),
		@NamedQuery(name = "IniFile.findByUpkFilename", query = "SELECT i FROM IniFile i WHERE i.iniFilename = :iniFilename"),
		@NamedQuery(name = "IniFile.findBySearchHashLength", query = "SELECT i FROM IniFile i WHERE i.searchHashLength = :searchHashLength"),
		@NamedQuery(name = "IniFile.findByBytesSum", query = "SELECT i FROM IniFile i WHERE i.bytesSum = :bytesSum") })
public class IniFile extends ModFile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;
	
	@Basic(optional = false)
	@Column(name = "iniFilename")
	private String iniFilename;
	
	@Basic(optional = false)
	@Column(name = "searchHashLength")
	private int searchHashLength;
	
	@Basic(optional = false)
	@Column(name = "bytesSum")
	private int bytesSum;
	
	@Basic(optional = false)
	@Lob
	@Column(name = "searchHash")
	private String searchHash;

	@Basic(optional = false)
	@Column(name = "isInstalled")
	private boolean isInstalled;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "module", fetch = FetchType.EAGER)
	private List<HexEdit> changes;
	
	@JoinColumn(name = "xmod", referencedColumnName = "id")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
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
		return "org.xcom.mod.entities.IniFile[ id=" + id + " ]";
	}

}
