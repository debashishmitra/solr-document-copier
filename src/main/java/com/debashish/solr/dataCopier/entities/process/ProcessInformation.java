/**
 * 
 */
package com.debashish.solr.dataCopier.entities.process;

import java.util.Date;

/**
 * @author Debashish Mitra
 *
 */
public class ProcessInformation {
	
	private Integer processId;
	
	private ProcessStatus processStatus;
	
	private Date startTime;
	
	private Date endTime;
	
	private Boolean isCompleted;

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(Integer processId) {
		this.processId = processId;
	}

	public ProcessStatus getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(ProcessStatus processStatus) {
		this.processStatus = processStatus;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}
}
