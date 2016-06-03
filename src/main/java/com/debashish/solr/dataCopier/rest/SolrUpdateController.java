package com.debashish.solr.dataCopier.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.debashish.solr.dataCopier.rest.requestObjects.DataSourceType;
import com.debashish.solr.dataCopier.rest.requestObjects.Request;
import com.debashish.solr.dataCopier.rest.requestObjects.SyncType;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateScope;
import com.debashish.solr.dataCopier.rest.responseObjects.SynchronizationResponse;
import com.debashish.solr.dataCopier.service.SolrService;
import com.debashish.solr.dataCopier.service.concurrent.MultiThreadedSolrService;
import com.debashish.solr.dataCopier.util.Util;

/**
 * 
 * @author Debashish Mitra
 *
 */
@RestController
@RequestMapping("/dataCopy")
public class SolrUpdateController {

	@Resource(name="multiThreadedSolrServiceImpl")
	private MultiThreadedSolrService multiThreadedSolrService;
	
	@Resource(name="solrServiceImpl")
	private SolrService solrService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String getIt() {
		return "The Solr Data Copier is deployed and running successfully ! You visited the Spring MVC controller";
	}

	@RequestMapping(path = "/from3CoreTo1Core", method = RequestMethod.POST)
	public Request from3CoreTo1Core(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to copy from 3 Core Solr to 1 core Solr ------------------------------------");
		int batchSize = request.getBatchSize();
		String sourceSolrBaseUrl = request.getSourceSolrUrl();
		String destinationSolrUrl = request.getDestinationSolrUrl();
		solrService.copyFrom3CoreSolrTo1CoreSolr(batchSize, sourceSolrBaseUrl, destinationSolrUrl);
		return request;
	}

	@RequestMapping(path = "/from1CoreTo1Core", method = RequestMethod.POST)
	public String from1CoreTo1Core(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to copy from 1 Core Solr to 1 core Solr ------------------------------------");
		String sourceSolrBaseUrl = request.getSourceSolrUrl();
		String destinationSolrUrl = request.getDestinationSolrUrl();
		int batchSize = request.getBatchSize();
		solrService.copyFrom1CoreSolrto1CoreSolr(sourceSolrBaseUrl, destinationSolrUrl, batchSize);
		return "Process Finished";
	}

	@RequestMapping(path = "/fromDatabaseTo1Core", method = RequestMethod.POST)
	public String fromDatabaseTo1Core(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to copy data from the Database to Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			setDataSource(request);
		}
		UpdateScope updateScope = request.getUpdateScope() == null ? UpdateScope.FULL : request.getUpdateScope();
		UpdateInterval updateInterval = request.getUpdateInterval();
		SyncType syncType = request.getSyncType();
		if(UpdateScope.INTERVAL_BASED.equals(updateScope) && updateInterval == null) {
			return "Error : Please provide UpdateInterval values for interval based updates";
		}
		String message = solrService.importToSolrFromDataBase(request.getBatchSize(), destinationSolrUrl, syncType, updateScope, updateInterval);
		return message;
	}
	
	@RequestMapping(path = "/fromDatabaseTo1CoreMultithreaded", method = RequestMethod.POST)
	public String fromDatabaseTo1CoreMultithreaded(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to copy data from the Database to Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			setDataSource(request);
		}
		
		UpdateScope updateScope = request.getUpdateScope() == null ? UpdateScope.FULL : request.getUpdateScope();
		UpdateInterval updateInterval = request.getUpdateInterval();
		SyncType syncType = request.getSyncType();
		
		if(UpdateScope.INTERVAL_BASED.equals(updateScope) && updateInterval == null) {
			return "Error : Please provide UpdateInterval values for interval based updates";
		}
		
		int batchSize = request.getBatchSize();
		int concurrency = request.getConcurrency();
		
		multiThreadedSolrService.multiThreadedImport(batchSize, destinationSolrUrl, syncType, updateScope, updateInterval, concurrency);
		
		return "Task Submitted";
	}
	
	@RequestMapping(path = "/deleteExtraFromSolr", method = RequestMethod.POST)
	public String deleteExtraFromSolr(@RequestBody Request request) throws SolrServerException, IOException {
		System.out.println("-------------------------- Received start request to delete extra records from Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			if(request.getUserDataSource() == null) {
				return "Error : \"userDataSource\" field (having user data source information) is required if \"dataSoutceType\":\"User\"";
			}
			setDataSource(request);
		}
		int count = solrService.deleteExtraDocumentsFromSolr(destinationSolrUrl);
		return count + " documents deleted from Solr";
	}
	
	@RequestMapping(path = "/fromDatabaseTo1Core/selectedPids", method = RequestMethod.POST)
	public String fromDatabaseTo1CoreForSelectedPids(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to copy selected Pids from the Database to Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			if(request.getUserDataSource() == null) {
				return "Error : User Data source information is required if \"dataSoutceType\":\"User\"";
			}
			setDataSource(request);
		}
		List<String> pidIds = request.getPidIdList();
		solrService.importOnlySelectedPidsToSolrFromDataBase(request.getBatchSize(), destinationSolrUrl, pidIds);
		return "Process Finished";
	}
	
	@RequestMapping(path = "/fromDatabaseTo1Core/selectedPidColors", method = RequestMethod.POST)
	public String fromDatabaseTo1CoreForSelectedPidColors(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to copy selected Pid Colors from the Database to Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			if(request.getUserDataSource() == null) {
				return "Error : User Data source information is required if \"dataSoutceType\":\"User\"";
			}
			setDataSource(request);
		}
		List<String> pidColorsIds = request.getPidColorIdList();
		solrService.importOnlySelectedPidColorsToSolrFromDataBase(request.getBatchSize(), destinationSolrUrl, pidColorsIds);
		return "Process Finished";
	}

	@RequestMapping(path = "/pidColorRowCount", method = RequestMethod.GET)
	public Long getPidColorRowCount() {
		System.out.println("-------------------------- Received start request for pidColorRowCount ------------------------------------");
		Long pidColorRowCount = solrService.getPidColorRowCount();
		return pidColorRowCount;
	}

	@RequestMapping(path = "/pidRowCount", method = RequestMethod.GET)
	public Long getPidRowCount() {
		System.out.println("-------------------------- Received start request for pidRowCount ------------------------------------");
		Long pidRowCount = solrService.getPidRowCount();
		return pidRowCount;
	}

	@RequestMapping("/pidColorIdToMarketingIdListMap")
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMap() {
		System.out.println("----------------------------- getPidColorIdToMarketingIdListMap() -----------------------");
		return solrService.getPidColorIdToMarketingIdListMap();
	}
	
	@RequestMapping(path = "/syncComparingIdsOnly", method = RequestMethod.POST)
	public SynchronizationResponse synchronizeSolrWithDatabase(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to synchronize Solr to Database ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			setDataSource(request);
		}
		SynchronizationResponse info = solrService.synchronizeSolrWithDatabaseBasedOnIdComparisionOnly(destinationSolrUrl);
		return info;
	}
	
	@RequestMapping(path = "/syncComparingAllFields", method = RequestMethod.POST)
	public SynchronizationResponse synchronizeSolrToDatabaseOnFullDataComparision(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to synchronize Solr to Database ------------------------------------");
		//Util.getDataSource("jdbc:oracle:thin:@AINBIZ01DB:1521/AINBIZ02", "ocprtbiza", "ocprtbiza1")
		String solrServerBaseUrl = request.getDestinationSolrUrl();
		int batchSize = request.getBatchSize();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			setDataSource(request);
		}
		SynchronizationResponse info = solrService.synchronizeSolrToDatabaseOnFullDataComparision(batchSize, solrServerBaseUrl);
		return info;
	}
	
	@RequestMapping(path = "/generateDifferenceReports", method = RequestMethod.POST)
	public String generateDifferenceReports(@RequestBody Request request) {
		System.out.println("-------------------------- Received start request to generate difference reports between Solr and Database ------------------------------------");
		//Util.getDataSource("jdbc:oracle:thin:@AINBIZ01DB:1521/AINBIZ02", "ocprtbiza", "ocprtbiza1")
		String solrServerBaseUrl = request.getDestinationSolrUrl();
		int batchSize = request.getBatchSize();
		String folder = request.getReportFolderPath();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			setDataSource(request);
		}
		solrService.generateDifferenceReports(solrServerBaseUrl, folder, batchSize);
		return "0";
	}
	
	private void setDataSource(Request request) {
		String dbConnectionString = request.getUserDataSource().getDatabaseConnectionString();
		String dbUsername  = request.getUserDataSource().getUsername();
		String dbPassword  = request.getUserDataSource().getPassword();
		try {
			DataSource ds = Util.getBasicDataSourceForOracleConnection(dbConnectionString, dbUsername, dbPassword);
			solrService.setDataSource(ds);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}