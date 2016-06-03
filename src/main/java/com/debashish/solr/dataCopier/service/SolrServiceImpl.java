package com.debashish.solr.dataCopier.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.debashish.solr.dataCopier.dao.Dao;
import com.debashish.solr.dataCopier.entities.Core0;
import com.debashish.solr.dataCopier.entities.Core1;
import com.debashish.solr.dataCopier.entities.Core2;
import com.debashish.solr.dataCopier.entities.ProductSolrDocument;
import com.debashish.solr.dataCopier.rest.requestObjects.SyncType;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateScope;
import com.debashish.solr.dataCopier.rest.responseObjects.SynchronizationResponse;
import com.debashish.solr.dataCopier.util.Util;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;

/**
 * @author Debashish Mitra
 * 
 */
@Service("solrServiceImpl")
public class SolrServiceImpl implements SolrService {

	public static final int DEFAUT_READ_BATCH_SIZE = 10000;
	public static final int DEFAUT_UPDATE_BATCH_SIZE = 10000;
	public static final int DEFAUT_WRITE_BATCH_SIZE = 10000;
	public static final int MINIMUM_BATCH_SIZE = 100;
	
	@Resource(name="springDaoImpl")
	private Dao dao;

	public SolrServiceImpl() {

	}

	@Transactional
	public void addDocuments() {
		String sourceSolrServerURL = "http://esu1l333:43180/solr";
		String destinationSolrServerURL = "http://localhost:8080/solr";
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setParam("q", "*:*");
		for (int i = 0; i < 3; i++) {
			try {
				transferSolrDataForCore(i, sourceSolrServerURL, solrQuery, destinationSolrServerURL, Class.forName("com.debashish.solr.product.SolrCore" + i + "DTO"));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Transactional
	public void addDataToNewCollection1FromOld3Cores() {
		addDataToNewCollection1FromOld3Cores("http://esu1l333:43180/solr", "http://localhost:8080/solr/collection1");
	}

	@Transactional
	public void addDataToNewCollection1FromOld3Cores(String sourceSolrServerURLBase, String destinationSolrServerURL) {

	}

	@Transactional
	public <T> List<T> getDTOListByReadingFromSolrInBatches(String serverUrl, int batchSize, Class<T> dtoClass) {
		SolrServer solrServer = new HttpSolrServer(serverUrl);
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setParam("q", "*:*");
		List<T> dtoList = new ArrayList<T>();
		try {
			QueryResponse queryResponse = solrServer.query(solrQuery);
			long numfound = queryResponse.getResults().getNumFound();
			long numberOfCallsToMake = getNumberOfCallsToMake(numfound, batchSize);

			for (int i = 0; i < numberOfCallsToMake; i++) {
				solrQuery.setParam("start", (new Integer(i * batchSize)).toString());
				solrQuery.setParam("rows", String.valueOf(batchSize));
				queryResponse = solrServer.query(solrQuery);
				List<T> solrCoreDTOList = queryResponse.getBeans(dtoClass);
				dtoList.addAll(solrCoreDTOList);
				System.out.println("--------------------------------- Number of rows read from " + dtoClass.getCanonicalName() + " is " + dtoList.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("--------------------------------- Reading " + dtoClass.getCanonicalName() + " over. Number of rows read from " + dtoClass.getCanonicalName() + " is " + dtoList.size());
		return dtoList;
	}

	@Transactional
	public Map<String, Core1> getPidToPidChannelMap(String serverUrl, int batchSize) {
		Map<String, Core1> pidToPidChannelMap = new HashMap<String, Core1>();
		List<Core1> solrCore1DTOList = getDTOListByReadingFromSolrInBatches(serverUrl, 100000, Core1.class);
		for (Core1 solrCore1DTO : solrCore1DTOList) {
			if (solrCore1DTO.getPidId() != null) {
				pidToPidChannelMap.put(solrCore1DTO.getPidId(), solrCore1DTO);
			}
		}
		solrCore1DTOList.clear();
		solrCore1DTOList = null;
		return pidToPidChannelMap;
	}

	@Transactional
	public Map<String, Core2> getPidColorToPidColorChannelMap(String serverUrl, int batchSize) {
		Map<String, Core2> pidColorToPidColorChannelMap = new HashMap<String, Core2>();
		List<Core2> solrCore2DTOList = getDTOListByReadingFromSolrInBatches(serverUrl, 100000, Core2.class);
		for (Core2 solrCore2DTO : solrCore2DTOList) {
			if (solrCore2DTO.getPidColorId() != null) {
				pidColorToPidColorChannelMap.put(solrCore2DTO.getPidColorId(), solrCore2DTO);
			}
		}
		solrCore2DTOList.clear();
		solrCore2DTOList = null;
		return pidColorToPidColorChannelMap;
	}

	@Transactional
	public List<ProductSolrDocument> removeBadRecords(List<ProductSolrDocument> in) {
		List<ProductSolrDocument> out = new ArrayList<ProductSolrDocument>();
		for (ProductSolrDocument solrCollection1DTO : in) {
			if (solrCollection1DTO.getPidColor() != null) {
				out.add(solrCollection1DTO);
			}
		}
		return out;
	}

	@Transactional
	public void updateDocuments() {
		String solrServerURL = "http://esu3v081.federated.fds:8280/solr";
		SolrQuery solrQuery = new SolrQuery();

		solrQuery.setParam("q", "*:*");

		updateCore0(solrServerURL, solrQuery);
	}

	@Transactional
	public long getNumberOfCallsToMake(long numfound, long batchSize) {
		return (numfound % batchSize) == 0 ? (numfound / batchSize) : ((numfound / batchSize) + 1);
	}

	@Transactional
	public void updateCore0(String solrServerURL, SolrQuery solrQuery) {
		SolrServer solrServer = new HttpSolrServer(solrServerURL + "/core0");
		QueryResponse queryResponse = null;
		long numfound = 0;
		List<Core0> solrCore0AllDTOList = new ArrayList<Core0>();
		try {
			queryResponse = solrServer.query(solrQuery);
			numfound = queryResponse.getResults().getNumFound();
			long numberOfCallsToMake = getNumberOfCallsToMake(numfound, DEFAUT_READ_BATCH_SIZE);

			for (int i = 0; i < numberOfCallsToMake; i++) {
				solrQuery.setParam("start", (new Integer(i * DEFAUT_READ_BATCH_SIZE)).toString());
				solrQuery.setParam("rows", "10000");
				queryResponse = solrServer.query(solrQuery);
				List<Core0> solrCore0DTOList = queryResponse.getBeans(Core0.class);
				solrCore0AllDTOList.addAll(solrCore0DTOList);
			}

			System.out.println("Total rec = " + solrCore0AllDTOList.size());

			int numberOfPIDsUpdated = 0;
			for (Core0 solrCore0DTO : solrCore0AllDTOList) {
				String convertedPID = convertToHyphenatedForm(solrCore0DTO.getPidId());
				if (convertedPID != null && !convertedPID.equalsIgnoreCase(solrCore0DTO.getPidId())) {
					solrCore0DTO.setPidId(convertedPID);
					solrCore0DTO.setId(convertedPID);
					numberOfPIDsUpdated++;
				}
			}
			System.out.println(" ++++++++++++++++++++++++++++++++ Number of PIDs Updated = " + numberOfPIDsUpdated + " ++++++++++++++++++++++++++++++++++++++++++++++++++ ");

			solrServer.deleteByQuery("*:*");
			int numberOfRecordsUpdated = 0;
			List<Core0> batch = new ArrayList<Core0>();
			for (Core0 solrCore0DTO : solrCore0AllDTOList) {
				batch.add(solrCore0DTO);
				if (batch.size() == DEFAUT_UPDATE_BATCH_SIZE) {
					solrServer.addBeans(batch);
					solrServer.commit();
					numberOfRecordsUpdated += DEFAUT_UPDATE_BATCH_SIZE;
					batch.clear();
				}
			}
			if (batch.size() > 0) {
				solrServer.addBeans(batch);
				solrServer.commit();
				numberOfRecordsUpdated += batch.size();
				batch.clear();
			}
			System.out.println(" ++++++++++++++++++++++++++++++++ Number of Records Updated = " + numberOfRecordsUpdated + " ++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public void updateCore1(String solrServerURL, SolrQuery solrQuery) {
		SolrServer solrServer = new HttpSolrServer(solrServerURL + "/core1");
		QueryResponse queryResponse = null;
		long numfound = 0;
		List<Core1> solrCore1AllDTOList = new ArrayList<Core1>();
		try {
			queryResponse = solrServer.query(solrQuery);
			numfound = queryResponse.getResults().getNumFound();
			long numberOfCallsToMake = getNumberOfCallsToMake(numfound, DEFAUT_READ_BATCH_SIZE);

			for (int i = 0; i < numberOfCallsToMake; i++) {
				solrQuery.setParam("start", (new Integer(i * DEFAUT_READ_BATCH_SIZE)).toString());
				solrQuery.setParam("rows", "10000");
				queryResponse = solrServer.query(solrQuery);
				List<Core1> solrCore1DTOList = queryResponse.getBeans(Core1.class);
				solrCore1AllDTOList.addAll(solrCore1DTOList);
			}

			System.out.println("Total rec = " + solrCore1AllDTOList.size());

			int numberOfPIDsUpdated = 0;
			for (Core1 solrCore1DTO : solrCore1AllDTOList) {
				String convertedPID = convertToHyphenatedForm(solrCore1DTO.getPidId());
				if (convertedPID != null && !convertedPID.equalsIgnoreCase(solrCore1DTO.getPidId())) {
					solrCore1DTO.setPidId(convertedPID);
					solrCore1DTO.setId(convertedPID);
					numberOfPIDsUpdated++;
				}
			}
			System.out.println(" ++++++++++++++++++++++++++++++++ Number of PIDs Updated = " + numberOfPIDsUpdated + " ++++++++++++++++++++++++++++++++++++++++++++++++++ ");

			solrServer.deleteByQuery("*:*");
			int numberOfRecordsUpdated = 0;
			List<Core1> batch = new ArrayList<Core1>();
			for (Core1 solrCore1DTO : solrCore1AllDTOList) {
				batch.add(solrCore1DTO);
				if (batch.size() == DEFAUT_UPDATE_BATCH_SIZE) {
					solrServer.addBeans(batch);
					solrServer.commit();
					numberOfRecordsUpdated += DEFAUT_UPDATE_BATCH_SIZE;
					batch.clear();
				}
			}
			if (batch.size() > 0) {
				solrServer.addBeans(batch);
				solrServer.commit();
				numberOfRecordsUpdated += batch.size();
				batch.clear();
			}
			System.out.println(" ++++++++++++++++++++++++++++++++ Number of Records Updated = " + numberOfRecordsUpdated + " ++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public void updateCore2(String solrServerURL, SolrQuery solrQuery) {
		SolrServer solrServer = new HttpSolrServer(solrServerURL + "/core2");
		QueryResponse queryResponse = null;
		long numfound = 0;
		List<Core2> solrCore2AllDTOList = new ArrayList<Core2>();
		try {
			queryResponse = solrServer.query(solrQuery);
			numfound = queryResponse.getResults().getNumFound();
			long numberOfCallsToMake = getNumberOfCallsToMake(numfound, DEFAUT_READ_BATCH_SIZE);

			for (int i = 0; i < numberOfCallsToMake; i++) {
				solrQuery.setParam("start", (new Integer(i * DEFAUT_READ_BATCH_SIZE)).toString());
				solrQuery.setParam("rows", "10000");
				queryResponse = solrServer.query(solrQuery);
				List<Core2> solrCore2DTOList = queryResponse.getBeans(Core2.class);
				solrCore2AllDTOList.addAll(solrCore2DTOList);
			}

			System.out.println("Total rec = " + solrCore2AllDTOList.size());

			int numberOfPIDsUpdated = 0;
			for (Core2 solrCore2DTO : solrCore2AllDTOList) {
				String convertedPID = convertToHyphenatedForm(solrCore2DTO.getPidColorId());
				if (convertedPID != null && !convertedPID.equalsIgnoreCase(solrCore2DTO.getPidColorId())) {
					solrCore2DTO.setPidColorId(convertedPID);
					solrCore2DTO.setId(convertedPID);
					numberOfPIDsUpdated++;
				}
			}
			System.out.println(" ++++++++++++++++++++++++++++++++ Number of PIDs Updated = " + numberOfPIDsUpdated + " ++++++++++++++++++++++++++++++++++++++++++++++++++ ");

			solrServer.deleteByQuery("*:*");
			int numberOfRecordsUpdated = 0;
			List<Core2> batch = new ArrayList<Core2>();
			for (Core2 solrCore2DTO : solrCore2AllDTOList) {
				batch.add(solrCore2DTO);
				if (batch.size() == DEFAUT_UPDATE_BATCH_SIZE) {
					solrServer.addBeans(batch);
					solrServer.commit();
					numberOfRecordsUpdated += DEFAUT_UPDATE_BATCH_SIZE;
					batch.clear();
				}
			}
			if (batch.size() > 0) {
				solrServer.addBeans(batch);
				solrServer.commit();
				numberOfRecordsUpdated += batch.size();
				batch.clear();
			}
			System.out.println(" ++++++++++++++++++++++++++++++++ Number of Records Updated = " + numberOfRecordsUpdated + " ++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String convertToHyphenatedForm(String input) {
		if (!StringUtils.isEmpty(input) && input.length() == 32) {
			input = input.toLowerCase();
			String part1 = input.substring(0, 8);
			String part2 = input.substring(8, 12);
			String part3 = input.substring(12, 16);
			String part4 = input.substring(16, 20);
			String part5 = input.substring(20, 32);
			String finalString = part1 + "-" + part2 + "-" + part3 + "-" + part4 + "-" + part5;
			return finalString;
		}
		return input;
	}

	public void main(String[] args) {
		SolrServiceImpl s = new SolrServiceImpl();
		s.addDataToNewCollection1FromOld3Cores();
	}

	@Transactional
	public <T> void transferSolrDataForCore(int coreNumber, String sourceSolrServerURL, SolrQuery solrQuery, String destinationSolrServerUrl, Class<T> dtoClass) {
		transferSolrDataForCore("core" + coreNumber, sourceSolrServerURL, solrQuery, destinationSolrServerUrl, dtoClass);
	}

	public <T> void transferSolrDataForCore(String coreNameArgument, String sourceSolrServerURL, SolrQuery solrQuery, String destinationSolrServerUrl, Class<T> dtoClass) {
		String coreName = "/" + coreNameArgument;
		SolrServer sourceSolrServer = new HttpSolrServer(sourceSolrServerURL + coreName);
		QueryResponse queryResponse = null;
		long numfound = 0;
		List<T> solrCoreAllDTOList = new ArrayList<T>();
		try {
			queryResponse = sourceSolrServer.query(solrQuery);
			numfound = queryResponse.getResults().getNumFound();
			long numberOfCallsToMake = getNumberOfCallsToMake(numfound, DEFAUT_READ_BATCH_SIZE);

			for (int i = 0; i < numberOfCallsToMake; i++) {
				solrQuery.setParam("start", (new Integer(i * DEFAUT_READ_BATCH_SIZE)).toString());
				solrQuery.setParam("rows", "10000");
				queryResponse = sourceSolrServer.query(solrQuery);
				List<T> solrCoreDTOList = queryResponse.getBeans(dtoClass);
				solrCoreAllDTOList.addAll(solrCoreDTOList);
			}

			System.out.println("Total rec = " + solrCoreAllDTOList.size());

			SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl + coreName);
			destinationSolrServer.deleteByQuery("*:*");
			int numberOfRecordsUpdated = 0;
			List<T> batch = new ArrayList<T>();
			for (T solrCoreDTO : solrCoreAllDTOList) {
				batch.add(solrCoreDTO);
				if (batch.size() == DEFAUT_UPDATE_BATCH_SIZE) {
					try {
						destinationSolrServer.addBeans(batch);
						destinationSolrServer.commit();
					} catch (Exception e) {
						e.printStackTrace();
					}
					numberOfRecordsUpdated += DEFAUT_UPDATE_BATCH_SIZE;
					batch.clear();
				}
			}
			if (batch.size() > 0) {
				try {
					destinationSolrServer.addBeans(batch);
					destinationSolrServer.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
				numberOfRecordsUpdated += batch.size();
				batch.clear();
			}
			System.out.println(" ++++++++++++++++++++++++++++++++ Number of Records Updated = " + numberOfRecordsUpdated + " ++++++++++++++++++++++++++++++++++++++++++++++++++ ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	@Override
	public void copyFrom3CoreSolrTo1CoreSolr(int batchSize, String sourceSolrServerUrl, String destinationSolrServerUrl) {

		destinationSolrServerUrl = destinationSolrServerUrl + "/collection1";

		Map<String, Core1> pidToPidChannelMap = new HashMap<String, Core1>();
		Map<String, Core2> pidColorToPidColorChannelMap = new HashMap<String, Core2>();

		// --------------------------------------------------------------

		String core1URL = sourceSolrServerUrl + "/core1";
		System.out.println("---------------------------------------- Starting to read from core1 100000 at a time --------------------------------------");
		pidToPidChannelMap = getPidToPidChannelMap(core1URL, batchSize);

		System.gc();
		System.out.println("Total number of entries in PidToPidChannelMap (built from solr core1) = " + pidToPidChannelMap.size());

		// ---------------------------------------------------------------------------------------

		String core2URL = sourceSolrServerUrl + "/core2";
		System.out.println("---------------------------------------- Starting to read from core2 100000 at a time --------------------------------------");
		pidColorToPidColorChannelMap = getPidColorToPidColorChannelMap(core2URL, batchSize);

		System.gc();
		System.out.println("Total number of entries in PidColorToPidColorChannelMap (built from solr core2) = " + pidColorToPidColorChannelMap.size());

		// ------------------------------------------------------------------------------------------------------

		String core0URL = sourceSolrServerUrl + "/core0";

		SolrServer solrServer = new HttpSolrServer(core0URL);
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setParam("q", "*:*");
		solrQuery.setParam("sort", "_docid_ asc");
		QueryResponse queryResponse = null;
		long numfound = 0;
		try {
			queryResponse = solrServer.query(solrQuery);
			numfound = queryResponse.getResults().getNumFound();
			long numberOfCallsToMake = getNumberOfCallsToMake(numfound, batchSize);
			SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
			System.out.println("--------------------------- Deleting existing documents from collection1 --------------------------------");
			destinationSolrServer.deleteByQuery("*:*");
			System.out.println("--------------------------- Starting data copy (indexing of collection1) of " + numfound + " rows " + batchSize + " at a time --------------------------------");
			for (int i = 0; i < numberOfCallsToMake; i++) {
				solrQuery.setParam("start", (new Integer(i * batchSize)).toString());
				solrQuery.setParam("rows", String.valueOf(batchSize));
				queryResponse = solrServer.query(solrQuery);
				List<ProductSolrDocument> solrCollection1DTOList = queryResponse.getBeans(ProductSolrDocument.class);
				System.out.println("Total number of records in batch : " + solrCollection1DTOList.size());

				for (ProductSolrDocument solrCollection1DTO : solrCollection1DTOList) {
					if (pidToPidChannelMap.containsKey(solrCollection1DTO.getPidId())) {
						try {
							solrCollection1DTO.setPidChannel(pidToPidChannelMap.get(solrCollection1DTO.getPidId()).getChannelCode());
							solrCollection1DTO.setMasterStyleId(pidToPidChannelMap.get(solrCollection1DTO.getPidId()).getMasterStyleId());
							solrCollection1DTO.setDivisionId(pidToPidChannelMap.get(solrCollection1DTO.getPidId()).getDivisionId());
							pidToPidChannelMap.remove(solrCollection1DTO.getPidId());
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					if (pidColorToPidColorChannelMap.containsKey(solrCollection1DTO.getId())) {
						try {
							solrCollection1DTO.setPidColorChannel(pidColorToPidColorChannelMap.get(solrCollection1DTO.getId()).getChannelCode());
							solrCollection1DTO.setSubclassId(pidColorToPidColorChannelMap.get(solrCollection1DTO.getId()).getSubclassId());
							pidColorToPidColorChannelMap.remove(solrCollection1DTO.getId());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				destinationSolrServer.addBeans(solrCollection1DTOList);
				destinationSolrServer.commit();
				if (i < numberOfCallsToMake - 1) {
					System.out.println("----------------------------- Number of indices copied = " + ((i + 1) * DEFAUT_WRITE_BATCH_SIZE) + "-------------------------------------------");
				}
				System.gc();
			}
			solrQuery.setParam("start", "0");
			solrQuery.setParam("rows", "1");
			Long totalInSolr = destinationSolrServer.query(solrQuery).getResults().getNumFound();
			System.out.println("----------------------------- INDEXING FINISHED - Number of indices copied = " + totalInSolr + "-------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println();
	}

	@Transactional
	@Override
	public void copyFrom1CoreSolrto1CoreSolr(String sourceSolrServerUrl, String destinationSolrServerUrl, int batchSize) {

		sourceSolrServerUrl = sourceSolrServerUrl + "/collection1";
		destinationSolrServerUrl = destinationSolrServerUrl + "/collection1";
		if (!(batchSize > 0)) {
			batchSize = DEFAUT_WRITE_BATCH_SIZE;
		}
		SolrServer solrServer = new HttpSolrServer(sourceSolrServerUrl);
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setParam("q", "*:*");
		solrQuery.setParam("sort", "_docid_ asc");
		QueryResponse queryResponse = null;
		long numfound = 0;
		try {
			queryResponse = solrServer.query(solrQuery);
			numfound = queryResponse.getResults().getNumFound();
			long numberOfCallsToMake = getNumberOfCallsToMake(numfound, batchSize);
			SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
			System.out.println("--------------------------- Deleting existing documents from collection1 --------------------------------");
			destinationSolrServer.deleteByQuery("*:*");
			System.out.println("--------------------------- Starting data copy (indexing of collection1) of " + numfound + " rows " + batchSize + " at a time --------------------------------");
			for (int i = 0; i < numberOfCallsToMake; i++) {
				solrQuery.setParam("start", (new Integer(i * batchSize)).toString());
				solrQuery.setParam("rows", String.valueOf(batchSize));
				queryResponse = solrServer.query(solrQuery);
				List<ProductSolrDocument> solrCollection1DTOList = queryResponse.getBeans(ProductSolrDocument.class);
				System.out.println("Total number of records in batch : " + solrCollection1DTOList.size());

				destinationSolrServer.addBeans(solrCollection1DTOList);
				destinationSolrServer.commit();
				if (i < numberOfCallsToMake - 1) {
					System.out.println("----------------------------- Number of indices copied = " + ((i + 1) * batchSize) + "-------------------------------------------");
				}
				System.gc();
			}
			solrQuery.setParam("start", "0");
			solrQuery.setParam("rows", "1");
			Long totalInSolr = destinationSolrServer.query(solrQuery).getResults().getNumFound();
			System.out.println("----------------------------- INDEXING FINISHED - Number of indices copied = " + totalInSolr + "-------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println();
	}

	@Transactional
	@Override
	public String importToSolrFromDataBase(int batchSize, String solrServerBaseUrl, SyncType syncType, UpdateScope updateScope, UpdateInterval updateInterval) {
		// deleteAllFromSolrBeforeCopying=true -> Intrusive mode (Solr is not usable while copy is going on) - This does a clean copy by deleting all documents before starting to copy
		// deleteAllFromSolrBeforeCopying=false -> Non-Intrusive mode (Solr is usable while copy is going on) - First it deletes any records that are extra in Solr. Then it starts to overwrites all documents in Solr with the latest values from the Database 
		try {
			int numberOfDocumentsIndexed = 0;
			int numberOfRecordsDeletedFromSolr = 0;
			
			if(SyncType.NON_INTRUSIVE_OVERWRITE.equals(syncType)) {
				//Delete extra from Solr
				System.out.println("--------------------------- Deleting extra documents from collection1 --------------------------------");
				numberOfRecordsDeletedFromSolr = deleteExtraDocumentsFromSolr(solrServerBaseUrl);
			}
	
			String destinationSolrServerUrl = solrServerBaseUrl + "/collection1";
			SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
			
			if (SyncType.CLEAN_COPY.equals(syncType)) {
				System.out.println("--------------------------- Deleting existing documents from collection1 --------------------------------");
				destinationSolrServer.deleteByQuery("*:*");
			}
			
			System.out.println("--------------------------- Starting data copy (indexing of collection1) " + batchSize + " at a time --------------------------------");
			
			Map<String, List<Long>> pidColorIdToMarketingIdListMap = getPidColorIdToMarketingIdListMap();
			
			batchSize=  (batchSize < 100) ? DEFAUT_WRITE_BATCH_SIZE : batchSize;
			int first = 1;
			int last = batchSize;
			long a = System.currentTimeMillis();
			while(true) {
				System.out.println("About to call DB");
				List<ProductSolrDocument> solrCollection1DTOList = null;
				if(updateScope == UpdateScope.INTERVAL_BASED) {
					solrCollection1DTOList = dao.getProductSolrCollection1DataFromDatabaseForTheSpecifiedRangeAndElapsedTime(updateInterval, new Long(first), new Long(last));
				} else
					//updateScope == UpdateScope.Full
				{
					solrCollection1DTOList = dao.getProductSolrCollection1DataFromDatabaseForTheSpecifiedRange(new Long(first), new Long(last));
				}
				if(solrCollection1DTOList.size() ==0) {
					break;
				}
				System.out.println("Fetched Data from DB");
				System.out.println("Total number of records in batch fetched from DB : " + solrCollection1DTOList.size());
				addMarketingAssociationInformation(solrCollection1DTOList, pidColorIdToMarketingIdListMap);
				System.out.println("About to Send " + solrCollection1DTOList.size() + " documents to Solr");
				destinationSolrServer.addBeans(solrCollection1DTOList);
				UpdateResponse updateResponse = destinationSolrServer.commit();
				if(updateResponse.getStatus() == 0) {
					numberOfDocumentsIndexed+=solrCollection1DTOList.size();
				}
				System.out.println("Sent " + solrCollection1DTOList.size() + " documents to Solr");
				first+=batchSize;
				last+=batchSize;
				System.gc();
			}
			
			long b = System.currentTimeMillis();
			System.out.println("----------------------------- INDEXING FINISHED - Number of indices copied = " + numberOfDocumentsIndexed + " -------------------------------------------");
			System.out.println("++++++++++++++++ Time taken for data copy to Solr = " + Util.getTimeStringFromMilliseconds(b-a));
			System.out.println("++++++++++++++++ Number of documents found extra in Solr and deleted = " + numberOfRecordsDeletedFromSolr);
			String message = "Synchronization finished - Number of indices copied = " + numberOfDocumentsIndexed + "\nTime taken for data copy to Solr = " + Util.getTimeStringFromMilliseconds(b-a) + "\nNumber of documents found extra in Solr and deleted = " + numberOfRecordsDeletedFromSolr;
			return message;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	@Transactional
	public SynchronizationResponse synchronizeSolrToDatabaseOnFullDataComparision(int batchSize, String solrServerBaseUrl) {
		batchSize = (batchSize < 100) ? DEFAUT_WRITE_BATCH_SIZE : batchSize;
		int firstRowNumber = 1;
		int lastRowNumber = batchSize;
		int totalMismatchCount = 0;
		Map<String, List<Long>> pidColorIdToMarketingIdListMap = getPidColorIdToMarketingIdListMap();
		/*
		 * Compare each record from Solr and DB and update the records in Solr that are out of sync (indicated by non matching field values)
		 */
		while(true) {
			List<ProductSolrDocument> solrCollection1DTOListFromDB = dao.getProductSolrCollection1DataFromDatabaseForTheSpecifiedRange(new Long(firstRowNumber), new Long(lastRowNumber));
			if (solrCollection1DTOListFromDB.size() == 0) {
				break;
			}
			//Add Marketing Id data to the records fetched from the Database
			addMarketingAssociationInformation(solrCollection1DTOListFromDB, pidColorIdToMarketingIdListMap);
			
			List<String> pidColorIdListFromDb = new ArrayList<String>();
			
			for(ProductSolrDocument productSolrDocument : solrCollection1DTOListFromDB) {
				pidColorIdListFromDb.add(productSolrDocument.getId());
			}
			List<ProductSolrDocument> documentsFromSolr = getProductSolrCollection1DataFromSolrForSpecifiedPidColorIds(solrServerBaseUrl, pidColorIdListFromDb);
			
			solrCollection1DTOListFromDB.removeAll(documentsFromSolr);		
			
			if (!CollectionUtils.isEmpty(solrCollection1DTOListFromDB)) {
				addDocumentsToSolr(solrServerBaseUrl, solrCollection1DTOListFromDB);
				System.out.println("Number of out of sync records from " +  + firstRowNumber + " to " + lastRowNumber + " is " + solrCollection1DTOListFromDB.size());
				totalMismatchCount+=solrCollection1DTOListFromDB.size();
			} else {
				System.out.println("No out of sync records in " + firstRowNumber + " to " + lastRowNumber);
			}
			firstRowNumber += batchSize;
			lastRowNumber += batchSize;
		}
		
		System.out.println("Solr update Finished. Number of out of sync records were : " + totalMismatchCount + ". Now starting to delete extra records from Solr if any");
		
		//Deletion start
		
		Set<String> extraInSolr = getPidColorIdSetFromSolr(solrServerBaseUrl);
		
		Set<String> pidColorIdSetFromDatabase = getPidColorIdSetFromDatabase();
		
		extraInSolr.removeAll(pidColorIdSetFromDatabase);
		
		int numberOfRecordsDeletedFromSolr = 0;
		
		if (extraInSolr.size() > 0) {
			try {
				System.out.println("============ Found " + extraInSolr.size() + " extra records in Solr. About to remove them " + "====================================");
				numberOfRecordsDeletedFromSolr = verifyAndSafeDeleteSelectedPidColorsFromSolr(solrServerBaseUrl, new ArrayList<String>(extraInSolr));
				System.out.println("============ " + numberOfRecordsDeletedFromSolr + " records deleted from Solr ======================= ");
			} catch (SolrServerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Deletion end
		} else {
			System.out.println("============ Found no extra records in Solr ====================================");
		}
		
		String message = "++++++++++++++++++++++++++++++++++++++ Synchronization finished - Missing/non-matching from Solr : " + totalMismatchCount + ", " + "Extra in Solr : " + extraInSolr.size()+ ", " + "Number of records added to Solr : " + totalMismatchCount + ", " + "Number of records deleted from Solr : " + numberOfRecordsDeletedFromSolr;
		System.out.println(message);
		SynchronizationResponse synchronizationResponse = new SynchronizationResponse();
		synchronizationResponse.setExtraInSolr(extraInSolr.size());
		synchronizationResponse.setMissingFromSolr(totalMismatchCount);
		synchronizationResponse.setNumberOfRecordsAddedToSolr(totalMismatchCount);
		synchronizationResponse.setNumberOfRecordsDeletedFromSolr(numberOfRecordsDeletedFromSolr);
		synchronizationResponse.setMessage(message);
		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(synchronizationResponse));
		return synchronizationResponse;
	}
	
	@Override
	public List<ProductSolrDocument> addMarketingAssociationInformation(List<ProductSolrDocument> solrDocuments, Map<String, List<Long>> pidColorIdToMarketingIdListMap) {
		for (ProductSolrDocument solrDocument : solrDocuments) {
			solrDocument.setMarketingId(pidColorIdToMarketingIdListMap.get(solrDocument.getId()));
		}
		return solrDocuments;
	}

	@Override
	public Long getPidRowCount() {
		return dao.getPidRowCount();
	}

	@Override
	public Long getPidColorRowCount() {
		return dao.getPidColorRowCount();
	}

	@Override
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMap() {
		return dao.getPidColorIdToMarketingIdListMap();
	}

	@Override
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPids(List<String> pidIds) {
		return dao.getPidColorIdToMarketingIdListMapForSelectedPids(pidIds);
	}

	@Override
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPidColors(List<String> pidColorIds) {
		return dao.getPidColorIdToMarketingIdListMapForSelectedPidColors(pidColorIds);
	}

	@Override
	public void importOnlySelectedPidsToSolrFromDataBase(int batchSize, String destinationSolrServerUrl, List<String> pidIds) {
		List<List<String>> batches = Lists.partition(pidIds, batchSize);
		System.out.println("--------------------------- Starting data copy (indexing of collection1) of " + pidIds.size() + " PIDs " + batchSize + " at a time --------------------------------");
		destinationSolrServerUrl+= "/collection1";
		SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
		int totalDocumentsCopiedCount = 0;
		Map<String, List<Long>> pidColorIdToMarketingIdListMap = null;
		if(batches.size() > 1) {
			pidColorIdToMarketingIdListMap = getPidColorIdToMarketingIdListMap();
		}
		for (List<String> pids : batches) {
			try {
				if(batches.size() == 1) {
					pidColorIdToMarketingIdListMap = getPidColorIdToMarketingIdListMapForSelectedPids(pids);
				}
				
				System.out.println("------------------------------------------------ About to call Database ------------------------------------------------");
				List<ProductSolrDocument> solrCollection1DTOList = dao.getProductSolrCollection1DataForSelectedPidIds(pids);
				if (!solrCollection1DTOList.isEmpty()) {
					int totalNumberOfRecordsInThisBatch = solrCollection1DTOList.size();
					System.out.println("------------------------------------------------ Fetched Data from DB ------------------------------------------------");
					System.out.println("------------------------------------------------ Total number of records in batch fetched from DB : " + totalNumberOfRecordsInThisBatch + " ------------------------------------------------");
					addMarketingAssociationInformation(solrCollection1DTOList, pidColorIdToMarketingIdListMap);
					System.out.println("------------------------------------------------ About to Send " + solrCollection1DTOList.size() + " PIDs to Solr");
					destinationSolrServer.addBeans(solrCollection1DTOList);
					destinationSolrServer.commit();
					totalDocumentsCopiedCount += totalNumberOfRecordsInThisBatch;

					System.out.println("------------------------------------------------ Sent " + totalNumberOfRecordsInThisBatch + " PIDs in this batch to Solr");
					System.out.println("------------------------------------------------ Sent a total of " + totalDocumentsCopiedCount + " PIDs to Solr");
					
					System.gc();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("----------------------------- INDEXING FINISHED - Number of indices copied = " + totalDocumentsCopiedCount + "-------------------------------------------");
	}

	@Override
	@Transactional
	public int importOnlySelectedPidColorsToSolrFromDataBase(int batchSize, String destinationSolrServerBaseUrl, List<String> pidColorIds) {
		List<List<String>> batches = Lists.partition(pidColorIds, batchSize);
		System.out.println("--------------------------- Starting data copy (indexing of collection1) of " + pidColorIds.size() + " PIDs " + batchSize + " at a time --------------------------------");
		String destinationSolrServerUrl = destinationSolrServerBaseUrl + "/collection1";
		SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
		int totalDocumentsCopiedCount = 0;
		for (List<String> pidColors : batches) {
			try {
				Map<String, List<Long>> pidColorIdToMarketingIdListMap = getPidColorIdToMarketingIdListMapForSelectedPidColors(pidColors);
				System.out.println("About to call Database");
				List<ProductSolrDocument> solrCollection1DTOList = dao.getProductSolrCollection1DataForSelectedPidColorIds(pidColors);
				if (!solrCollection1DTOList.isEmpty()) {
					int totalNumberOfRecordsInThisBatch = solrCollection1DTOList.size();
					System.out.println("Fetched Data from DB");
					System.out.println("Total number of records in batch fetched from DB : " + totalNumberOfRecordsInThisBatch);
					addMarketingAssociationInformation(solrCollection1DTOList, pidColorIdToMarketingIdListMap);
					System.out.println("About to Send " + solrCollection1DTOList.size() + " PID Colors to Solr");
					destinationSolrServer.addBeans(solrCollection1DTOList);
					destinationSolrServer.commit();
					totalDocumentsCopiedCount += totalNumberOfRecordsInThisBatch;

					System.out.println("Sent " + totalNumberOfRecordsInThisBatch + " PID Colors in this batch to Solr");
					System.out.println("Sent a total of " + totalDocumentsCopiedCount + " PID Colors to Solr");
					
					System.gc();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("----------------------------- INDEXING FINISHED - Number of indices copied = " + totalDocumentsCopiedCount + "-------------------------------------------");
		return totalDocumentsCopiedCount;
	}
	
	@Override
	@Transactional
	public SynchronizationResponse synchronizeSolrWithDatabaseBasedOnIdComparisionOnly(String solrServerBaseUrl) {
		
		Set<String> pidColorIdSetFromSolr = getPidColorIdSetFromSolr(solrServerBaseUrl);

		Set<String> pidColorIdSetFromDatabase = getPidColorIdSetFromDatabase();

		Set<String> missingFromSolr = new HashSet<String>(pidColorIdSetFromDatabase);

		missingFromSolr.removeAll(pidColorIdSetFromSolr);

		int numberOfRecordsAddedToSolr = importOnlySelectedPidColorsToSolrFromDataBase(300, solrServerBaseUrl, new ArrayList<String>(missingFromSolr));
		
		Set<String> extraInSolr = new HashSet<String>(pidColorIdSetFromSolr);

		extraInSolr.removeAll(pidColorIdSetFromDatabase);
		
		int numberOfRecordsDeletedFromSolr = 0;
		try {
			numberOfRecordsDeletedFromSolr = verifyAndSafeDeleteSelectedPidColorsFromSolr(solrServerBaseUrl, new ArrayList<String>(extraInSolr));
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String message = "Missing from Solr : " + missingFromSolr.size() + ", " + "Extra in Solr : " + extraInSolr.size()+ ", " + "Number of records added to Solr : " + numberOfRecordsAddedToSolr+ ", " + "Number of records deleted from Solr : " + numberOfRecordsDeletedFromSolr;
		
		SynchronizationResponse r = new SynchronizationResponse();
		r.setExtraInSolr(extraInSolr.size());
		r.setMissingFromSolr(missingFromSolr.size());
		r.setNumberOfRecordsAddedToSolr(numberOfRecordsAddedToSolr);
		r.setNumberOfRecordsDeletedFromSolr(numberOfRecordsDeletedFromSolr);
		r.setMessage(message);
		return r;
	}

	@Override
	@Transactional
	public Set<String> getPidColorIdSetFromSolr(String solrServerBaseUrl) {

		String destinationSolrServerUrl = solrServerBaseUrl + "/collection1";
		SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);

		SolrQuery solrQuery = new SolrQuery();

		solrQuery.setParam("q", "*:*");
		solrQuery.setParam("start", "0");
		solrQuery.setParam("rows", "100000000");
		solrQuery.setParam("fl", "id");
		solrQuery.setQuery("*:*");
		Set<String> pidColorIdSetFromSolr = new HashSet<String>();
		try {
			QueryResponse q = destinationSolrServer.query(solrQuery);
			SolrDocumentList solrDocumentList = q.getResults();
			for(SolrDocument solrDocument : solrDocumentList) {
				pidColorIdSetFromSolr.add(solrDocument.getFieldValue("id").toString());
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return pidColorIdSetFromSolr;
	}

	@Transactional
	@Override
	public Set<String> getPidColorIdSetFromDatabase() {
		Set<String> pidColorIdSetFromDatabase = dao.getFullPidColorIdSetFromDatabase();
		return pidColorIdSetFromDatabase;
	}
	
	/**
	 * This method verifies with the database that the PID colors requested for deletion from Solr
	 * are actually still not in Database since we don not want records which are in DB to be deleted from Solr.
	 * This method ensures that if any of the IDs which were previously reported to be in Solr and not in the DB
	 * are now in DB then they are removed from the delete list (i.e the list of IDs of the documents that are to be deleted from Solr)
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	@Transactional
	@Override
	public int verifyAndSafeDeleteSelectedPidColorsFromSolr(String destinationSolrServerBaseUrl, List<String> pidColorIds) throws SolrServerException, IOException {
		List<String> presentPidColorIdSetFromDB = new ArrayList<String> (dao.getPresentPidColorIdSet(pidColorIds));
		pidColorIds.removeAll(presentPidColorIdSetFromDB);
		return deleteSelectedPidColorsFromSolr(destinationSolrServerBaseUrl, pidColorIds);
	}
	

	@Transactional
	@Override
	public int deleteSelectedPidsFromSolr(String destinationSolrServerBaseUrl, List<String> pidIds) throws SolrServerException, IOException {
		List<List<String>> batches = Lists.partition(pidIds, 1000);
		int totalNumberofRecordsDeletedFromSolr = 0;
		String destinationSolrServerUrl = destinationSolrServerBaseUrl + "/collection1";
		SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
		for (List<String> pidIdList : batches) {
			if (pidIdList != null && pidIdList.size() > 0) {
				String deleteQuery = Util.getSolrInQuery("pidId",pidIdList);
				System.out.println("Delete Query - " + deleteQuery);
				destinationSolrServer.deleteByQuery(deleteQuery);
				UpdateResponse solrDeleteQueryResponse = destinationSolrServer.commit();
				int status = solrDeleteQueryResponse.getStatus();
				if (status == 0) {
					totalNumberofRecordsDeletedFromSolr += pidIdList.size();
				}
			}
		}
		return totalNumberofRecordsDeletedFromSolr;
	}
	
	@Transactional
	@Override
	public int deleteSelectedPidColorsFromSolr(String destinationSolrServerBaseUrl, List<String> pidColorIds) throws SolrServerException, IOException {
		List<List<String>> batches = Lists.partition(pidColorIds, 1000);
		int totalNumberofRecordsDeletedFromSolr = 0;
		String destinationSolrServerUrl = destinationSolrServerBaseUrl + "/collection1";
		SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
		for (List<String> pidColorIdList : batches) {
			if (pidColorIdList != null && pidColorIdList.size() > 0) {
				String deleteQuery = Util.getSolrInQuery("id",pidColorIdList);
				System.out.println("Delete Query - " + deleteQuery);
				destinationSolrServer.deleteByQuery(deleteQuery);
				UpdateResponse solrDeleteQueryResponse = destinationSolrServer.commit();
				int status = solrDeleteQueryResponse.getStatus();
				if (status == 0) {
					totalNumberofRecordsDeletedFromSolr += pidColorIdList.size();
				}
			}
		}
		return totalNumberofRecordsDeletedFromSolr;
	}
	
	@Transactional
	@Override
	public List<ProductSolrDocument> getProductSolrCollection1DataFromSolrForSpecifiedPidColorIds(String solrServerBaseUrl, List<String> pidColorIds) {
		List<ProductSolrDocument> documentsFromSolrFullList = new ArrayList<ProductSolrDocument>();

		String destinationSolrServerUrl = solrServerBaseUrl + "/collection1";
		SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);

		SolrQuery solrQuery = new SolrQuery();

		solrQuery.setParam("q", "*:*");
		solrQuery.setParam("start", "0");
		solrQuery.setParam("rows", "100000000");
		List<List<String>> batches = Lists.partition(pidColorIds, 100);
		for (List<String> batch : batches) {
			solrQuery.setQuery(Util.getSolrInQuery("id", batch));
			try {
				QueryResponse q = destinationSolrServer.query(solrQuery);
				List<ProductSolrDocument> documentsFromSolr = q.getBeans(ProductSolrDocument.class);
				documentsFromSolrFullList.addAll(documentsFromSolr);
			} catch (SolrServerException e) {
				e.printStackTrace();
			}
		}
		return documentsFromSolrFullList;
	}

	@Transactional
	@Override
	public void addDocumentsToSolr(String solrServerBaseUrl, List<ProductSolrDocument> documents) {
		String destinationSolrServerUrl = solrServerBaseUrl + "/collection1";
		SolrServer destinationSolrServer = new HttpSolrServer(destinationSolrServerUrl);
		try {
			destinationSolrServer.addBeans(documents);
			destinationSolrServer.commit();
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Transactional
	@Override
	public void generateDifferenceReports(String solrServerBaseUrl, String reportFolderPath, int batchSize) {
		
		String timeStampedReportFolderPath = reportFolderPath + File.separator + System.currentTimeMillis();
		
		double beforeProcessStart = System.currentTimeMillis();
		
		//Start generating report
		
		System.out.println("--------------------------------------------- Starting the process to find field level differences in records between Solr and DB ---------------------------------------------");
		
		Map<String, List<Long>> pidColorIdToMarketingIdListMap = getPidColorIdToMarketingIdListMap();
		
		batchSize = batchSize < MINIMUM_BATCH_SIZE ? MINIMUM_BATCH_SIZE : batchSize;
		int firstRowNumber = 1;
		int lastRowNumber = batchSize;
		/*
		 * Compare each record from Solr and DB and make a report the records in Solr that are out of sync (indicated by non matching field values)
		 */
		StringBuilder summary = new StringBuilder();
		int totalNumberOfRecordsOutOfSync = 0;
		String discrepancyReportFilePath = timeStampedReportFolderPath + File.separator + "discrepancyReport";
		File discrepancyReportFile = new File(discrepancyReportFilePath);
		
		double beforeReportStart = System.currentTimeMillis();
		
		System.out.println("-------------------------------------------------- Starting comparision in batches of  " + batchSize + " --------------------------------------------------");
		while(true) {
			System.out.println("Batch  " + firstRowNumber + " to " + lastRowNumber);
			int numberOfRecordsOutOfSyncInThisBatch = 0;
			List<ProductSolrDocument> solrCollection1DTOListFromDB = dao.getProductSolrCollection1DataFromDatabaseForTheSpecifiedRange(new Long(firstRowNumber), new Long(lastRowNumber));
			if (solrCollection1DTOListFromDB.size() == 0) {
				break;
			}
			addMarketingAssociationInformation(solrCollection1DTOListFromDB, pidColorIdToMarketingIdListMap);
			List<String> pidColorIdListFromDb = new ArrayList<String>();
			
			Map<String, ProductSolrDocument> mapForDb = new HashMap<String, ProductSolrDocument> ();
			
			for(ProductSolrDocument productSolrDocument : solrCollection1DTOListFromDB) {
				pidColorIdListFromDb.add(productSolrDocument.getId());
				mapForDb.put(productSolrDocument.getId(), productSolrDocument);
			}
			
			List<ProductSolrDocument> documentsFromSolr = getProductSolrCollection1DataFromSolrForSpecifiedPidColorIds(solrServerBaseUrl, pidColorIdListFromDb);
			Map<String, ProductSolrDocument> mapForSolr = new HashMap<String, ProductSolrDocument> ();
			for(ProductSolrDocument productSolrDocument : documentsFromSolr) {
				mapForSolr.put(productSolrDocument.getId(), productSolrDocument);
			}
			StringBuilder report = new StringBuilder();
			for(Entry<String, ProductSolrDocument> entry : mapForDb.entrySet()) {
				
				ProductSolrDocument recFromDb = entry.getValue();
				ProductSolrDocument docFromSolr = mapForSolr.get(entry.getKey());
				String line = compare(recFromDb, docFromSolr);
				if(line.length() > 0) {
					numberOfRecordsOutOfSyncInThisBatch++;
					report.append(line + "\n");
				}
			}
			totalNumberOfRecordsOutOfSync+=numberOfRecordsOutOfSyncInThisBatch;
			System.out.println("-------------------------------------------------- Number of records out of sync in records " + firstRowNumber + " to " + lastRowNumber + " = " + numberOfRecordsOutOfSyncInThisBatch + " --------------------------------------------------");
			firstRowNumber += batchSize;
			lastRowNumber += batchSize;
			Util.writeStringToExistingFile(discrepancyReportFile, report.toString(), true);
		}
		//Report generation completed
		
		//Start to find missing and extra documents list
		System.out.println("-------------------------------------------------- Getting list of PID Color IDs from Solr --------------------------------------------------");
		Set<String> pidColorIdSetFromSolr = getPidColorIdSetFromSolr(solrServerBaseUrl);
		System.out.println("-------------------------------------------------- Fetched list of PID Color IDs from Solr. Num of recs = " + pidColorIdSetFromSolr.size() + " --------------------------------------------------");

		System.out.println("-------------------------------------------------- Getting list of PID Color IDs from Database --------------------------------------------------");
		Set<String> pidColorIdSetFromDatabase = getPidColorIdSetFromDatabase();
		System.out.println("-------------------------------------------------- Fetched list of PID Color IDs from Database. Num of recs = " + pidColorIdSetFromDatabase.size() + " --------------------------------------------------");

		Set<String> missingFromSolr = new HashSet<String>(pidColorIdSetFromDatabase);
		missingFromSolr.removeAll(pidColorIdSetFromSolr);
		
		String missingPidColorIdsFromSolrFilePath = timeStampedReportFolderPath + File.separator + "missingPidColorIdsFromSolr";
		System.out.println("-------------------------------------------------- Found list of PID Color IDs missing from Solr. Find it in the file " + missingPidColorIdsFromSolrFilePath + " --------------------------------------------------");
		
		Util.writeCollectionToFile(missingPidColorIdsFromSolrFilePath, missingFromSolr, true);
		
		Set<String> extraInSolr = new HashSet<String>(pidColorIdSetFromSolr);
		extraInSolr.removeAll(pidColorIdSetFromDatabase);
		
		String extraPidColorIdsInSolrReportFilePath = timeStampedReportFolderPath + File.separator + "extraPidColorIdsInSolr";
		System.out.println("-------------------------------------------------- Found list of PID Color IDs which are extra in Solr. Find it in the file " + extraPidColorIdsInSolrReportFilePath + " --------------------------------------------------");
		
		Util.writeCollectionToFile(extraPidColorIdsInSolrReportFilePath, extraInSolr, true);
		
		//Finished to find missing and extra documents list
		
		double afterReportEnd = System.currentTimeMillis();

		System.out.println("\n\n\n================================= Process Finished ====================================\n\n\n");
		
		double timeTakenToMakeReport = afterReportEnd - beforeReportStart;
		double totalTimeTakenForTheProcess = afterReportEnd - beforeProcessStart;
		
		System.out.println("================================= Total time taken for running the process = " + totalTimeTakenForTheProcess + " =================================");
		System.out.println("================================= Time taken for making the report = " + timeTakenToMakeReport + " =================================");
		System.out.println("================================= Total number of records out of sync = " + totalNumberOfRecordsOutOfSync + " =================================");
		System.out.println("================================= Total number of records missing from Solr = " + missingFromSolr.size() + " =================================");
		System.out.println("================================= Total number of records extra in Solr = " + extraInSolr.size() + " =================================");
		
		summary.append("Total time taken for running the process = " + totalTimeTakenForTheProcess);
		summary.append("\nTime taken for making the report = " + timeTakenToMakeReport);
		summary.append("\nTotal number of records missing from Solr = " + missingFromSolr.size());
		summary.append("\nTotal number of records extra in Solr = " + extraInSolr.size());
		summary.append("\nTotal number of records out of sync = " + totalNumberOfRecordsOutOfSync);
		try {
			FileUtils.writeStringToFile(new File(discrepancyReportFilePath+"Summary"), summary.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String compare(ProductSolrDocument referenceFromDb, ProductSolrDocument fromSolr) {
		
		if (fromSolr!=null) {
			StringBuilder differenceReportEntry = new StringBuilder();
			if (!Util.nullSafeEquals(referenceFromDb.getPid(), fromSolr.getPid())) {
				differenceReportEntry.append("pid (DB="+referenceFromDb.getPid() + " , Solr=" + fromSolr.getPid() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getActiveFlag(), fromSolr.getActiveFlag())) {
				differenceReportEntry.append("activeFlag (DB="+referenceFromDb.getActiveFlag() + " , Solr=" + fromSolr.getActiveFlag() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getBrandCode(), fromSolr.getBrandCode())) {
				differenceReportEntry.append("brandCode (DB="+referenceFromDb.getBrandCode() + " , Solr=" + fromSolr.getBrandCode() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getClassId(), fromSolr.getClassId())) {
				differenceReportEntry.append("classId (DB="+referenceFromDb.getClassId() + " , Solr=" + fromSolr.getClassId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getDepartmentId(), fromSolr.getDepartmentId())) {
				differenceReportEntry.append("departmentId (DB="+referenceFromDb.getDepartmentId() + " , Solr=" + fromSolr.getDepartmentId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getDivisionId(), fromSolr.getDivisionId())) {
				differenceReportEntry.append("divisionId (DB="+referenceFromDb.getDivisionId() + " , Solr=" + fromSolr.getDivisionId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getIsMerchApproved(), fromSolr.getIsMerchApproved())) {
				differenceReportEntry.append("isMerchApproved (DB="+referenceFromDb.getIsMerchApproved() + " , Solr=" + fromSolr.getIsMerchApproved() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getIsPIDActiveFlag(), fromSolr.getIsPIDActiveFlag())) {
				differenceReportEntry.append("isPIDActiveFlag (DB="+referenceFromDb.getIsPIDActiveFlag() + " , Solr=" + fromSolr.getIsPIDActiveFlag() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getIsPIDColorPlaceHolder(), fromSolr.getIsPIDColorPlaceHolder())) {
				differenceReportEntry.append("isPIDColorPlaceHolder (DB="+referenceFromDb.getIsPIDColorPlaceHolder() + " , Solr=" + fromSolr.getIsPIDColorPlaceHolder() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getIsPIDPlaceHolder(), fromSolr.getIsPIDPlaceHolder())) {
				differenceReportEntry.append("isPIDPlaceHolder (DB="+referenceFromDb.getIsPIDPlaceHolder() + " , Solr=" + fromSolr.getIsPIDPlaceHolder() + ") ");
			}
//			if (!Util.nullSafeEquals(referenceFromDb.getIsPlaceHolder(), fromSolr.getIsPlaceHolder())) {
//				differenceReportEntry.append("isPlaceHolder " + ") ");
//			}
			if (!Util.nullSafeEqualsForLists(referenceFromDb.getMarketingId(), fromSolr.getMarketingId())) {
				differenceReportEntry.append("marketingId (DB="+referenceFromDb.getMarketingId() + " , Solr=" + fromSolr.getMarketingId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getMasterStyleId(), fromSolr.getMasterStyleId())) {
				differenceReportEntry.append("masterStyleId (DB="+referenceFromDb.getMasterStyleId() + " , Solr=" + fromSolr.getMasterStyleId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getNrfColorDesc(), fromSolr.getNrfColorDesc())) {
				differenceReportEntry.append("nrfColorDesc (DB="+referenceFromDb.getNrfColorDesc() + " , Solr=" + fromSolr.getNrfColorDesc() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getPidColor(), fromSolr.getPidColor())) {
				differenceReportEntry.append("pidColor (DB="+referenceFromDb.getPidColor() + " , Solr=" + fromSolr.getPidColor() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getPidDescription(), fromSolr.getPidDescription())) {
				differenceReportEntry.append("pidDescription (DB="+referenceFromDb.getPidDescription() + " , Solr=" + fromSolr.getPidDescription() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getPidId(), fromSolr.getPidId())) {
				differenceReportEntry.append("pidId (DB="+referenceFromDb.getPidId() + " , Solr=" + fromSolr.getPidId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getProductTypeId(), fromSolr.getProductTypeId())) {
				differenceReportEntry.append("productTypeId (DB="+referenceFromDb.getProductTypeId() + " , Solr=" + fromSolr.getProductTypeId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getSubclassId(), fromSolr.getSubclassId())) {
				differenceReportEntry.append("subclassId (DB="+referenceFromDb.getSubclassId() + " , Solr=" + fromSolr.getSubclassId() + ") ");
			}
			if (!Util.nullSafeEquals(referenceFromDb.getVendorId(), fromSolr.getVendorId())) {
				differenceReportEntry.append("vendorId (DB="+referenceFromDb.getVendorId() + " , Solr=" + fromSolr.getVendorId() + ") ");
			}
			differenceReportEntry = differenceReportEntry.length() > 0 ? new StringBuilder().append(referenceFromDb.getId()).append(" - ").append(differenceReportEntry) : differenceReportEntry;
			return differenceReportEntry.toString().trim();
		}
		return "";
	}

	@Override
	public void addDocumentsToSolrForSelectedPidIdsFromFile(Long batchSize, String filePath, String solrServerBaseUrl) throws FileNotFoundException {
		System.out.println("------------------------------------------------ Starting to read file " + filePath + " ------------------------------------------------");
		Scanner fileScanner = new Scanner(new FileInputStream(filePath));
		List<String> pidIdList = new ArrayList<String>();
		while(fileScanner.hasNextLine()) {
			String pidId = fileScanner.nextLine();
			if(!StringUtils.isEmpty(pidId)) {
				pidIdList.add(pidId);
			}
		}
		fileScanner.close();
		System.out.println("------------------------------------------------  Finished reading file " + filePath + " ------------------------------------------------");
		System.out.println("------------------------------------------------ Starting to add documents to Solr ------------------------------------------------");
		importOnlySelectedPidsToSolrFromDataBase(batchSize.intValue(), solrServerBaseUrl, pidIdList);
	}
	
	@Override
	public void addDocumentsToSolrForSelectedPidColorIdsFromFile(Long batchSize, String filePath, String solrServerBaseUrl) throws FileNotFoundException {
		System.out.println("Starting to read file " + filePath);
		Scanner fileScanner = new Scanner(new FileInputStream(filePath));
		List<String> pidColorIdList = new ArrayList<String>();
		while(fileScanner.hasNextLine()) {
			String pidColorId = fileScanner.nextLine();
			pidColorIdList.add(pidColorId);
		}
		fileScanner.close();
		System.out.println("Finished reading file " + filePath);
		System.out.println("Starting to add documents to Solr");
		importOnlySelectedPidColorsToSolrFromDataBase(batchSize.intValue(), solrServerBaseUrl, pidColorIdList);
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		dao.setDataSource(dataSource);
	}

	@Override
	public int deleteExtraDocumentsFromSolr(String solrServerBaseUrl) throws SolrServerException, IOException {
		System.out.println("----------------------------- About to search for and delete extra documents from Solr -------------------------------------------");
		Set<String> pidColorIdFullListFromSolr = getPidColorIdSetFromSolr(solrServerBaseUrl);
		Set<String> pidColorIdFullListFromDb = getPidColorIdSetFromDatabase();
		pidColorIdFullListFromSolr.removeAll(pidColorIdFullListFromDb);
		Set<String> extraPidColorsInSolr = pidColorIdFullListFromSolr;
		int numberOfRecordsDeletedFromSolr = verifyAndSafeDeleteSelectedPidColorsFromSolr(solrServerBaseUrl, new ArrayList<String> (extraPidColorsInSolr));
		System.out.println("----------------------------- Extra documents deleted from Solr = " + numberOfRecordsDeletedFromSolr + "-------------------------------------------");
		return numberOfRecordsDeletedFromSolr;
	}
}