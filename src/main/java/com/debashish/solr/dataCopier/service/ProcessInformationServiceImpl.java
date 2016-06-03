package com.debashish.solr.dataCopier.service;

import org.springframework.stereotype.Service;

import com.debashish.solr.dataCopier.entities.process.ProcessInformation;
import com.debashish.solr.dataCopier.entities.process.ProcessInformationRegister;
import com.debashish.solr.dataCopier.exception.ProcessNotFoundException;

/**
 * @author Debashish Mitra
 *
 */
@Service
public class ProcessInformationServiceImpl implements ProcessInformationService {

	@Override
	public ProcessInformation getProcessInformation(int processId) {
		ProcessInformation processInformation = ProcessInformationRegister.getProcessInformation(processId);
		if(processInformation == null) {
			throw new ProcessNotFoundException();
		}
		return processInformation;
	}

}
