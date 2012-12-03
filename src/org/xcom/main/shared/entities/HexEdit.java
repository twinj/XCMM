/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xcom.main.shared.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * @author Daniel Kemp
 */
@Entity
@Table(name = "hexedit", catalog = "xmodstore", schema = "")
@XmlRootElement(name="HexEdit")
@XmlType(propOrder={"id", "offset", "data", "module"})
@NamedQueries({
		@NamedQuery(name = "HexEdit.findAll", query = "SELECT h FROM HexEdit h"),
		@NamedQuery(name = "HexEdit.findById", query = "SELECT h FROM HexEdit h WHERE h.id = :id"),
		@NamedQuery(name = "HexEdit.findByOffset", query = "SELECT h FROM HexEdit h WHERE h.offset = :offset") })
public class HexEdit implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;
	
	@Basic(optional = false)
	@Column(name = "offset")
	private int offset;
	
	@Basic(optional = false)
	@Lob
	@Column(name = "data")
	private String data;
	
	@JoinColumn(name = "module", referencedColumnName = "id")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private ResFile module;

	// Clean constructor required for serialisation
	public HexEdit() {
	}

	public HexEdit(Integer id) {
		this.id = id;
	}

	public HexEdit(Integer id, int offset, String data) {
		this.id = id;
		this.offset = offset;
		this.data = data;
	}

	@XmlAttribute(name = "Id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlElement(name="Offset")
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@XmlElement(name="Data")
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public ResFile getModule() {
		return module;
	}

	public void setModule(ResFile module) {
		this.module = module;
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
		if (!(object instanceof HexEdit)) {
			return false;
		}
		HexEdit other = (HexEdit) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.xcom.mod.entities.Change[ id=" + id + " ]";
	}

}
