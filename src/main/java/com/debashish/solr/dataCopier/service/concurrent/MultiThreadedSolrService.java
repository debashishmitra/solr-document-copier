package com.debashish.solr.dataCopier.service.concurrent;

import com.debashish.solr.dataCopier.rest.requestObjects.SyncType;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateScope;

/**
 * @author Debashish Mitra
 *
 */
public interface MultiThreadedSolrService {
	void multiThreadedImport(int batchSize, String solrServerBaseUrl, SyncType syncType, UpdateScope updateScope, UpdateInterval updateInterval, int concurrency);
}
