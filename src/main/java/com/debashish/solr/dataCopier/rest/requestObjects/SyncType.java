/**
 * 
 */
package com.debashish.solr.dataCopier.rest.requestObjects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author 026035
 *
 */
@XmlRootElement
public enum SyncType {
	@XmlElement CLEAN_COPY, @XmlElement NON_INTRUSIVE_OVERWRITE
}
