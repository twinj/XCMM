/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xcom.main.shared.entities;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Daniel Kemp
 */
@Entity
@Table(name = "modfile", catalog = "xmodstore", schema = "")
@XmlRootElement(name = "ResFile")
@XmlType(propOrder = {
		"id", "resName", "upkFilename", "checkSum", "searchHashLength",
		"searchHash", "changes", "XMod"
})
@NamedQueries({
		@NamedQuery(name = "ResFile.findAll", query = "SELECT r FROM ResFile r"),
		@NamedQuery(name = "ResFile.findById", query = "SELECT r FROM ResFile r WHERE r.id = :id"),
		@NamedQuery(name = "ResFile.findByUpkFilename", query = "SELECT r FROM ResFile r WHERE r.upkFilename = :upkFilename"),
		@NamedQuery(name = "ResFile.findBySearchHashLength", query = "SELECT r FROM ResFile r WHERE r.searchHashLength = :searchHashLength"),
		@NamedQuery(name = "ResFile.findByCheckSum", query = "SELECT r FROM ResFile r WHERE r.checkSum = :checkSum")
})
public class ResFile implements Serializable, ModXml {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;
	
	@Basic(optional = false)
	@Column(name = "upkFilename")
	private String upkFilename;
	
	@Basic(optional = false)
	@Column(name = "resName")
	private String resName;
	
	@Basic(optional = false)
	@Column(name = "searchHashLength")
	private int searchHashLength;
	
	@Basic(optional = false)
	@Column(name = "checkSum")
	private int checkSum;
	
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
	public ResFile() {}
	
	public ResFile(Integer id) {
		this.id = id;
	}
	
	public ResFile(Integer id, String resName, String filename,
			String searchHash, int hashLength, int sum) {
		this.id = id;
		this.resName = resName;
		this.upkFilename = filename;
		this.searchHash = searchHash;
		this.searchHashLength = hashLength;
		this.checkSum = sum;
	}
	
	@XmlAttribute(name = "Id")
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlElement(name = "ResourceName")
	public String getResName() {
		return resName;
	}
	
	public void setResName(String resName) {
		this.resName = resName;
	}
	
	@XmlElement(name = "UpkFilename")
	public String getUpkFilename() {
		return upkFilename;
	}
	
	public void setUpkFilename(String filename) {
		this.upkFilename = filename;
	}
	
	@XmlElement(name = "SearchHash")
	public String getSearchHash() {
		return searchHash;
	}
	
	public void setSearchHash(String searchHash) {
		this.searchHash = searchHash;
	}
	
	@XmlElement(name = "SearchHashLength")
	public int getSearchHashLength() {
		return searchHashLength;
	}
	
	public void setSearchHashLength(int hashLength) {
		this.searchHashLength = hashLength;
	}
	
	@XmlElement(name = "CheckSum")
	public int getCheckSum() {
		return checkSum;
	}
	
	public void setCheckSum(int sum) {
		this.checkSum = sum;
	}
	
	// @XmlElement(name = "IsInstalled")
	@XmlTransient
	public Boolean getIsInstalled() {
		return isInstalled;
	}
	
	public void setIsInstalled(Boolean b) {
		this.isInstalled = b;
	}
	
	@XmlElement(name = "XMod")
	public XMod getXMod() {
		return xMod;
	}
	
	public void setXMod(XMod xMod) {
		this.xMod = xMod;
	}
	
	// @XmlTransient - if go to SOAP or REST may need to re think
	@XmlElementWrapper(name = "Changes")
	@XmlElement(name = "HexEdit")
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
		if (!(object instanceof ResFile)) {
			return false;
		}
		ResFile other = (ResFile) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "org.xcom.mod.entities.UpkFile[ id=" + id + " ]";
	}
	
	@Override
	public Path getBasePath() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Path getXmlSavePath() {
		return null;
	}
	
	@Override
	public String getPrintName() {
		return "RESOURCE FILE";
	}
	
}
