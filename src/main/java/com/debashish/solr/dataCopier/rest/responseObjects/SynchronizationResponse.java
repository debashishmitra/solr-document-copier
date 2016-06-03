/**
 * 
 */
package com.debashish.solr.dataCopier.rest.responseObjects;

/**
 * @author Debashish Mitra
 *
 */
public class SynchronizationResponse {

	private Integer missingFromSolr;
	private Integer extraInSolr;
	private Integer numberOfRecordsAddedToSolr;
	private Integer numberOfRecordsDeletedFromSolr;
	private String message;
	private Integer numberOfRecordsUpdatedInSolrFromTheDatabase;

	public Integer getMissingFromSolr() {
		return missingFromSolr;
	}

	public void setMissingFromSolr(Integer missingFromSolr) {
		this.missingFromSolr = missingFromSolr;
	}

	public Integer getExtraInSolr() {
		return extraInSolr;
	}

	public void setExtraInSolr(Integer extraInSolr) {
		this.extraInSolr = extraInSolr;
	}

	public Integer getNumberOfRecordsAddedToSolr() {
		return numberOfRecordsAddedToSolr;
	}

	public void setNumberOfRecordsAddedToSolr(Integer numberOfRecordsAddedToSolr) {
		this.numberOfRecordsAddedToSolr = numberOfRecordsAddedToSolr;
	}

	public Integer getNumberOfRecordsDeletedFromSolr() {
		return numberOfRecordsDeletedFromSolr;
	}

	public void setNumberOfRecordsDeletedFromSolr(Integer numberOfRecordsDeletedFromSolr) {
		this.numberOfRecordsDeletedFromSolr = numberOfRecordsDeletedFromSolr;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getNumberOfRecordsUpdatedInSolrFromTheDatabase() {
		return numberOfRecordsUpdatedInSolrFromTheDatabase;
	}

	public void setNumberOfRecordsUpdatedInSolrFromTheDatabase(Integer numberOfRecordsUpdatedInSolrFromTheDatabase) {
		this.numberOfRecordsUpdatedInSolrFromTheDatabase = numberOfRecordsUpdatedInSolrFromTheDatabase;
	}
}
