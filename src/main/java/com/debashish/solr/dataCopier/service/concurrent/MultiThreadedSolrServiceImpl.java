package com.debashish.solr.dataCopier.service.concurrent;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debashish.solr.dataCopier.concurrent.task.ConcurrentSolrDataCopyTask;
import com.debashish.solr.dataCopier.dao.Dao;
import com.debashish.solr.dataCopier.rest.requestObjects.SyncType;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateScope;
import com.debashish.solr.dataCopier.service.SolrService;

/**
 * @author Debashish Mitra
 * 
 */
@Service("multiThreadedSolrServiceImpl")
public class MultiThreadedSolrServiceImpl implements MultiThreadedSolrService {

	public static final int DEFAUT_READ_BATCH_SIZE = 10000;
	public static final int DEFAUT_UPDATE_BATCH_SIZE = 10000;
	public static final int DEFAUT_WRITE_BATCH_SIZE = 10000;
	public static final int MINIMUM_BATCH_SIZE = 100;
	
	@Resource
	private Dao dao;
	
	@Resource(name="solrServiceImpl")
	private SolrService service;

	public MultiThreadedSolrServiceImpl() {

	}

	@Transactional
	@Override
	public void multiThreadedImport(final int batchSize, final String solrServerBaseUrl, final SyncType syncType, final UpdateScope updateScope, final UpdateInterval updateInterval, final int concurrency) {
		// deleteAllFromSolrBeforeCopying=true -> Intrusive mode (Solr is not usable while copy is going on) - This does a clean copy by deleting all documents before starting to copy
		// deleteAllFromSolrBeforeCopying=false -> Non-Intrusive mode (Solr is usable while copy is going on) - First it deletes any records that are extra in Solr. Then it starts to overwrites all documents in Solr with the latest values from the Database 
		try {
			final ExecutorService outerExecutorService = Executors.newCachedThreadPool();
			
			Future<Integer> deleteExtraResult = null;
			
			if (SyncType.NON_INTRUSIVE_OVERWRITE.equals(syncType)) {
				deleteExtraResult = outerExecutorService.submit(new Callable<Integer>() {
					@Override
					public Integer call() {
						// Delete extra from Solr
						System.out.println("--------------------------- Deleting extra documents from collection1 --------------------------------");
						try {
							return service.deleteExtraDocumentsFromSolr(solrServerBaseUrl);
						} catch (SolrServerException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}
				});
			}
			
			outerExecutorService.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					String destinationSolrServerUrl = solrServerBaseUrl + "/collection1";
					SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);

					if (SyncType.CLEAN_COPY.equals(syncType)) {
						System.out.println("--------------------------- Deleting existing documents from collection1 --------------------------------");
						destinationSolrServer.deleteByQuery("*:*");
						destinationSolrServer.commit();
					}

					System.out.println("--------------------------- Starting data copy (indexing of collection1) --------------------------------");

					Map<String, List<Long>> pidColorIdToMarketingIdListMap = dao.getPidColorIdToMarketingIdListMapConcurrently();

					AtomicInteger batchCounter = new AtomicInteger();
					AtomicBoolean hasMoreRecords = new AtomicBoolean(true);
					ExecutorService taskSpecificExecutorService = Executors.newCachedThreadPool();
					for (int i = 0; i < concurrency; i++) {
						ConcurrentSolrDataCopyTask t = new ConcurrentSolrDataCopyTask(batchCounter, batchSize, hasMoreRecords, solrServerBaseUrl, updateScope, updateInterval, pidColorIdToMarketingIdListMap, dao);
						taskSpecificExecutorService.submit(t);
					}
					taskSpecificExecutorService.shutdown();
					return null;
				}

			});
			outerExecutorService.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}