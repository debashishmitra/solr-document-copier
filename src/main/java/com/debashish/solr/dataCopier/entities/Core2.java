/**
 * 
 */
package com.debashish.solr.dataCopier.entities;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Debashish Mitra
 *
 */
@Deprecated
public class Core2 {
	@Field
	private String id;

	@Field
	private String pidColorId;

	@Field
	private String channelCode;

	@Field
	private Long subclassId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPidColorId() {
		return pidColorId;
	}

	public void setPidColorId(String pidColorId) {
		this.pidColorId = pidColorId;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public Long getSubclassId() {
		return subclassId;
	}

	public void setSubclassId(Long subclassId) {
		this.subclassId = subclassId;
	}
}
