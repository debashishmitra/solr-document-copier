/**
 * 
 */
package com.debashish.solr.dataCopier.rest.requestObjects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Debashish Mitra
 *
 */
@XmlRootElement
public class UpdateInterval {
	
	@XmlElement
	private UpdateIntervalUnit updateIntervalUnit;
	
	@XmlElement
	private Integer value;

	public UpdateIntervalUnit getUpdateIntervalUnit() {
		return updateIntervalUnit;
	}

	public void setUpdateIntervalUnit(UpdateIntervalUnit updateIntervalUnit) {
		this.updateIntervalUnit = updateIntervalUnit;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

}
