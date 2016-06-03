/**
 * 
 */
package com.debashish.solr.dataCopier.service;

import com.debashish.solr.dataCopier.entities.process.ProcessInformation;

/**
 * @author Debashish Mitra
 *
 */
public interface ProcessInformationService {

	public ProcessInformation getProcessInformation(int processId);
}
