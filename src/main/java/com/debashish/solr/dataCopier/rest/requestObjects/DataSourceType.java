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
public enum DataSourceType {
	@XmlElement USER, @XmlElement SYSTEM
}
