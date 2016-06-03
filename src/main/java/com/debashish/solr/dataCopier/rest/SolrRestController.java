package com.debashish.solr.dataCopier.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.stereotype.Component;

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
@Path("/dataCopyRest")
@Component
public class SolrRestController {

	@Resource(name="multiThreadedSolrServiceImpl")
	private MultiThreadedSolrService multiThreadedSolrService;
	
	@Resource(name="solrServiceImpl")
	private SolrService solrService;
	
	@GET
	@Path("/")
	public String getIt() {
		return "The Solr Data Copier is deployed and running successfully ! You visited the REST controller";
	}

	@POST
	@Path("/from3CoreTo1Core")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Request from3CoreTo1Core(Request request) {
		System.out.println("-------------------------- Received start request to copy from 3 Core Solr to 1 core Solr ------------------------------------");
		int batchSize = request.getBatchSize();
		String sourceSolrBaseUrl = request.getSourceSolrUrl();
		String destinationSolrUrl = request.getDestinationSolrUrl();
		solrService.copyFrom3CoreSolrTo1CoreSolr(batchSize, sourceSolrBaseUrl, destinationSolrUrl);
		return request;
	}
	
	@POST
	@Path("/from1CoreTo1Core")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String from1CoreTo1Core(Request request) {
		System.out.println("-------------------------- Received start request to copy from 1 Core Solr to 1 core Solr ------------------------------------");
		String sourceSolrBaseUrl = request.getSourceSolrUrl();
		String destinationSolrUrl = request.getDestinationSolrUrl();
		solrService.copyFrom1CoreSolrto1CoreSolr(sourceSolrBaseUrl, destinationSolrUrl, request.getBatchSize());
		return "Process Finished";
	}

	@POST
	@Path("/fromDatabaseTo1Core")
	public String fromDatabaseTo1Core(Request request) {
		System.out.println("-------------------------- Received start request to copy data from the Database to Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			if(request.getUserDataSource() == null) {
				return "Error : \"userDataSource\" field (having user data source information) is required if \"dataSoutceType\":\"User\"";
			}
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/fromDatabaseTo1CoreMultithreaded")
	public String fromDatabaseTo1CoreMultithreaded(Request request) {
		System.out.println("-------------------------- Received start request to copy data from the Database to Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			if(request.getUserDataSource() == null) {
				return "Error : \"userDataSource\" field (having user data source information) is required if \"dataSoutceType\":\"User\"";
			}
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/deleteExtraFromSolr")
	public String deleteExtraFromSolr(Request request) throws SolrServerException, IOException {
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

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/deleteSelectedPidsFromSolr")
	public String deleteSelectedPidsFromSolr(Request request) throws SolrServerException, IOException {
		System.out.println("-------------------------- Received start request to delete selected PIDs from Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			if(request.getUserDataSource() == null) {
				return "Error : \"userDataSource\" field (having user data source information) is required if \"dataSoutceType\":\"User\"";
			}
			setDataSource(request);
		}
		List<String> pidsToDelete = request.getPidIdList();
		int count = solrService.deleteSelectedPidsFromSolr(destinationSolrUrl, pidsToDelete);
		return count + " PIDs deleted from Solr";
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/deleteSelectedPidColorsFromSolr")
	public String deleteSelectedPidColorsFromSolr(Request request) throws SolrServerException, IOException {
		System.out.println("-------------------------- Received start request to delete selected PID Colors from Solr ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			if(request.getUserDataSource() == null) {
				return "Error : \"userDataSource\" field (having user data source information) is required if \"dataSoutceType\":\"User\"";
			}
			setDataSource(request);
		}
		List<String> pidColorsToDelete = request.getPidColorIdList();
		int count = solrService.deleteSelectedPidColorsFromSolr(destinationSolrUrl, pidColorsToDelete);
		return count + " PID Colors deleted from Solr";
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/fromDatabaseTo1Core/selectedPids")
	public String fromDatabaseTo1CoreForSelectedPids(Request request) {
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/fromDatabaseTo1Core/selectedPidColors")
	public String fromDatabaseTo1CoreForSelectedPidColors(Request request) {
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

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/pidColorRowCount")
	public Long getPidColorRowCount() {
		System.out.println("-------------------------- Received start request for pidColorRowCount ------------------------------------");
		Long pidColorRowCount = solrService.getPidColorRowCount();
		return pidColorRowCount;
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/pidRowCount")
	public Long getPidRowCount() {
		System.out.println("-------------------------- Received start request for pidRowCount ------------------------------------");
		Long pidRowCount = solrService.getPidRowCount();
		return pidRowCount;
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/pidColorIdToMarketingIdListMap")
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMap() {
		System.out.println("----------------------------- Received start request for getting PidColorId To MarketingId List Map -----------------------");
		return solrService.getPidColorIdToMarketingIdListMap();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/syncComparingIdsOnly")
	public SynchronizationResponse synchronizeSolrWithDatabase(Request request) {
		System.out.println("-------------------------- Received start request to synchronize Solr to Database ------------------------------------");
		String destinationSolrUrl = request.getDestinationSolrUrl();
		if(DataSourceType.USER.equals(request.getDataSourceType())) {
			setDataSource(request);
		}
		SynchronizationResponse info = solrService.synchronizeSolrWithDatabaseBasedOnIdComparisionOnly(destinationSolrUrl);
		return info;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/syncComparingAllFields")
	public SynchronizationResponse synchronizeSolrToDatabaseOnFullDataComparision(Request request) {
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/generateDifferenceReports")
	public String generateDifferenceReports(Request request) {
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
