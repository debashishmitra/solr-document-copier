/**
 * 
 */
package com.debashish.solr.dataCopier.exception;

/**
 * @author Debashish Mitra
 *
 */
public class ProcessNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1271498595253002129L;

	public ProcessNotFoundException() {
		super("No Process Found with the provided Process ID");
	}
}
