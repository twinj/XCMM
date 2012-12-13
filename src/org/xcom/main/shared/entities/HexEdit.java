/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xcom.main.shared.entities;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * @author Daniel Kemp
 */

@XmlRootElement(name="HexEdit")
@XmlType(propOrder={"id", "offset", "data", "module"})
public class HexEdit implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private int offset;
	private String data;
	private ResFile module;

	// Clean constructor required for serialisation
	public HexEdit() {}

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
		return "org.xcom.main.shared.entities.HexEdit[ id=" + id + " ]";
	}

}
