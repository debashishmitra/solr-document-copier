package com.debashish.solr.dataCopier.entities.process;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Debashish Mitra
 *
 */
public class ProcessInformationRegister extends ConcurrentHashMap<Integer, ProcessInformation> {
	
	private static final long serialVersionUID = 7180678163076404192L;
	
	private AtomicInteger processIdSequence = new AtomicInteger(0);
	
	private static ProcessInformationRegister processInformationRegister  = new ProcessInformationRegister();
	
	private ProcessInformationRegister() {}
	
	public static void putProcessInformation(ProcessInformation processInformation) {
		processInformationRegister.put(processInformation.getProcessId(), processInformation);
	}
	
	public static ProcessInformation getProcessInformation(Integer processId) {
		return processInformationRegister.get(processId);
	}
	
	public static ProcessInformationRegister get() {
		return processInformationRegister;
	}

	public AtomicInteger getProcessIdSequence() {
		return processIdSequence;
	}

	public void setProcessIdSequence(AtomicInteger processIdSequence) {
		this.processIdSequence = processIdSequence;
	}
}
