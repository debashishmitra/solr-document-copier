package com.debashish.solr.dataCopier.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.debashish.solr.dataCopier.dao.Dao;

/**
 * @author Debashish Mitra
 *
 */
public class MarketingIdAssociationDataFetcherTask implements Callable<List<Map<String, Object>>> {

	private AtomicInteger batchCounter;
	private Dao dao;
	private int batchSize;
	private AtomicBoolean hasMoreRecords;

	public MarketingIdAssociationDataFetcherTask(AtomicInteger batchCounter, int batchSize, AtomicBoolean hasMoreRecords, Dao dao) {
		this.batchCounter = batchCounter;
		this.batchSize = batchSize;
		this.hasMoreRecords=hasMoreRecords;
		this.dao = dao;
	}

	@Override
	public List<Map<String, Object>> call() throws Exception {
		List<Map<String, Object>> dataResultForThisTask = new ArrayList<Map<String, Object>>();
		try {
			batchSize = (batchSize < 100) ? 10000 : batchSize;
			while (hasMoreRecords.get()) {
				int batch = batchCounter.getAndIncrement();
				int firstRow = batch * batchSize + 1;
				int lastRow = firstRow + batchSize - 1;
				System.out.println("About to get records " + firstRow + " to " + lastRow + " in thread " + Thread.currentThread().getId() + " Batch Number - " + batch);
				List<Map<String, Object>> data = dao.getPidColorMarketingIdAssociationDataForTheSpecifiedRange(firstRow, lastRow);
				if(data.size() == 0) {
					hasMoreRecords.set(false);
					break;
				} else {
					dataResultForThisTask.addAll(data);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataResultForThisTask;
	}
}
