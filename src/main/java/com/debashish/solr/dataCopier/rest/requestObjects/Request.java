package com.debashish.solr.dataCopier.rest.requestObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Debashish Mitra
 *
 */
@XmlRootElement
public class Request {

	@XmlElement
	private int batchSize;
	@XmlElement
	private DataSourceType dataSourceType = DataSourceType.SYSTEM;
	@XmlElement
	private String sourceSolrUrl;
	@XmlElement
	private String destinationSolrUrl;
	@XmlElement
	private UserDataSource userDataSource;
	@XmlElement
	private List<String> pidIdList;
	@XmlElement
	private List<String> pidColorIdList;
	@XmlElement
	private String reportFolderPath;
	@XmlElement
	private UpdateScope updateScope;
	@XmlElement
	private UpdateInterval updateInterval;
	@XmlElement
	private SyncType syncType;
	@XmlElement
	private int concurrency;
	@XmlElement
	private List<String> validBrandsList;

	public Request() {}
	
	public String getSourceSolrUrl() {
		return sourceSolrUrl;
	}

	public void setSourceSolrUrl(String sourceSolrUrl) {
		this.sourceSolrUrl = sourceSolrUrl;
	}

	public String getDestinationSolrUrl() {
		return destinationSolrUrl;
	}

	public void setDestinationSolrUrl(String destinationSolrUrl) {
		this.destinationSolrUrl = destinationSolrUrl;
	}

	public UserDataSource getUserDataSource() {
		return userDataSource;
	}

	public void setUserDataSource(UserDataSource userDataSource) {
		this.userDataSource = userDataSource;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public DataSourceType getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(DataSourceType dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public String getReportFolderPath() {
		return reportFolderPath;
	}

	public void setReportFolderPath(String reportFolderPath) {
		this.reportFolderPath = reportFolderPath;
	}

	public List<String> getPidIdList() {
		return pidIdList;
	}

	public void setPidIdList(List<String> pidIdList) {
		this.pidIdList = pidIdList;
	}

	public List<String> getPidColorIdList() {
		return pidColorIdList;
	}

	public void setPidColorIdList(List<String> pidColorIdList) {
		this.pidColorIdList = pidColorIdList;
	}

	public UpdateScope getUpdateScope() {
		return updateScope;
	}

	public void setUpdateScope(UpdateScope updateScope) {
		this.updateScope = updateScope;
	}

	public UpdateInterval getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(UpdateInterval updateInterval) {
		this.updateInterval = updateInterval;
	}

	public SyncType getSyncType() {
		return syncType;
	}

	public void setSyncType(SyncType syncType) {
		this.syncType = syncType;
	}

	public int getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(int concurrency) {
		this.concurrency = concurrency;
	}

	public List<String> getValidBrandsList() {
		return validBrandsList;
	}

	public void setValidBrandsList(List<String> validBrandsList) {
		this.validBrandsList = validBrandsList;
	}
}
