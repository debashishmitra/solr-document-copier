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
public class Core1 {
	@Field
	private String id;

	@Field
	private String pidId;

	@Field
	private String channelCode;

	@Field
	private Long divisionId;

	@Field
	private Long masterStyleId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPidId() {
		return pidId;
	}

	public void setPidId(String pidId) {
		this.pidId = pidId;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public Long getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Long divisionId) {
		this.divisionId = divisionId;
	}

	public Long getMasterStyleId() {
		return masterStyleId;
	}

	public void setMasterStyleId(Long masterStyleId) {
		this.masterStyleId = masterStyleId;
	}
}
