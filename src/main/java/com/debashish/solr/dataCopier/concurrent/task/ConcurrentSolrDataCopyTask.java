package com.debashish.solr.dataCopier.concurrent.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;

import com.debashish.solr.dataCopier.dao.Dao;
import com.debashish.solr.dataCopier.entities.ProductSolrDocument;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateScope;
import com.debashish.solr.dataCopier.util.Util;

/**
 * @author Debashish Mitra
 *
 */
public class ConcurrentSolrDataCopyTask implements Callable<String> {

	private AtomicInteger batchCounter;
	private Dao dao;
	private int batchSize;
	private String solrServerBaseUrl;
	private UpdateScope updateScope;
	private UpdateInterval updateInterval;
	private Map<String, List<Long>> pidColorIdToMarketingIdListMap;
	private AtomicBoolean hasMoreRecords;

	public ConcurrentSolrDataCopyTask(AtomicInteger batchCounter, int batchSize, AtomicBoolean hasMoreRecords, String solrServerBaseUrl, UpdateScope updateScope, UpdateInterval updateInterval, Map<String, List<Long>> pidColorIdToMarketingIdListMap, Dao dao) {
		this.batchCounter = batchCounter;
		this.batchSize = batchSize;
		this.solrServerBaseUrl = solrServerBaseUrl;
		this.updateScope = updateScope;
		this.updateInterval = updateInterval;
		this.pidColorIdToMarketingIdListMap = pidColorIdToMarketingIdListMap;
		this.hasMoreRecords=hasMoreRecords;
		this.dao = dao;
	}

	@Override
	public String call() throws Exception {
		try {
			int numberOfDocumentsIndexed = 0;

			String destinationSolrServerUrl = solrServerBaseUrl + "/collection1";
			SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);

			batchSize = (batchSize < 100) ? 10000 : batchSize;

			long a = System.currentTimeMillis();
			while (hasMoreRecords.get()) {
				int batch = batchCounter.getAndIncrement();
				int firstRow = batch * batchSize + 1;
				int lastRow = firstRow + batchSize - 1;

				System.out.println("About to call DB");
				List<ProductSolrDocument> solrCollection1DTOList = null;
				System.out.println("About to get records " + firstRow + " to " + lastRow + " in thread " + Thread.currentThread().getId() + " Batch Number - " + batch);
				if (updateScope == UpdateScope.INTERVAL_BASED) {
					solrCollection1DTOList = dao.getProductSolrCollection1DataFromDatabaseForTheSpecifiedRangeAndElapsedTime(updateInterval, new Long(firstRow), new Long(lastRow));
				} else
				// updateScope == UpdateScope.Full
				{
					solrCollection1DTOList = dao.getProductSolrCollection1DataFromDatabaseForTheSpecifiedRange(new Long(firstRow), new Long(lastRow));
				}
				if (solrCollection1DTOList.size() == 0) {
					hasMoreRecords.set(false);
					break;
				}
				System.out.println("Fetched Data from DB. Total number of records in batch fetched from DB : " + solrCollection1DTOList.size());
				addMarketingAssociationInformation(solrCollection1DTOList, pidColorIdToMarketingIdListMap);
				System.out.println("About to Send " + solrCollection1DTOList.size() + " documents to Solr");
				destinationSolrServer.addBeans(solrCollection1DTOList);
				UpdateResponse updateResponse = destinationSolrServer.commit();
				if (updateResponse.getStatus() == 0) {
					numberOfDocumentsIndexed += solrCollection1DTOList.size();
				}
				System.out.println("Sent " + solrCollection1DTOList.size() + " documents to Solr");
				System.gc();
			}

			long b = System.currentTimeMillis();
			System.out.println("----------------------------- INDEXING FINISHED - Number of indices copied by thread " + Thread.currentThread().getId() + " = " + numberOfDocumentsIndexed + " -------------------------------------------");
			System.out.println("++++++++++++++++ Time taken for data copy to Solr = " + Util.getTimeStringFromMilliseconds(b - a));
			String message = "Synchronization finished - Number of indices copied = " + numberOfDocumentsIndexed + "\nTime taken for data copy to Solr = " + Util.getTimeStringFromMilliseconds(b - a);
			return message;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<ProductSolrDocument> addMarketingAssociationInformation(List<ProductSolrDocument> solrDocuments, Map<String, List<Long>> pidColorIdToMarketingIdListMap) {
		for (ProductSolrDocument solrDocument : solrDocuments) {
			solrDocument.setMarketingId(pidColorIdToMarketingIdListMap.get(solrDocument.getId()));
		}
		return solrDocuments;
	}
}
