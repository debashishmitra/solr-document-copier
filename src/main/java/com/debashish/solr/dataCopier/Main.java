package com.debashish.solr.dataCopier;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.debashish.solr.dataCopier.dao.Dao;
import com.debashish.solr.dataCopier.dao.SpringDaoImpl;
import com.debashish.solr.dataCopier.rest.requestObjects.SyncType;
import com.debashish.solr.dataCopier.service.SolrService;
import com.debashish.solr.dataCopier.service.SolrServiceImpl;
import com.debashish.solr.dataCopier.util.Util;

/**
 * @author Debashish Mitra
 *
 *	This class was once used provided on demand to run this application as a standalone. This has not been maintained and possibly will need updates to be usable with the latest version of this application
 */
public class Main {

	private static ApplicationContext standaloneApplicationContext = new ClassPathXmlApplicationContext("classpath*:application-context-standalone.xml");
	
	public static void main(String[] args) throws IOException {
		
		String operation = args[0];
		String propertiesFileContainingFolderPath = args[1];
		String environment = args[2];
		String connectionPropertiesFilePath = propertiesFileContainingFolderPath + File.separator + "connection.properties";
		//Read properties file to get properties
		Properties connectionProperties = Util.getPropertiesFromFile(connectionPropertiesFilePath);
		String dbConnectionUrl = connectionProperties.getProperty(environment + ".db-connection-string");
		String username = connectionProperties.getProperty(environment + ".db-username");
		String password = connectionProperties.getProperty(environment + ".db-password");
		String solrServerUrl = connectionProperties.getProperty(environment + ".solr-server-url");
		try {
			setup(dbConnectionUrl,  username,  password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String operationPropertiesFilePath = propertiesFileContainingFolderPath + File.separator + "operation.properties";
		Properties operationProperties = Util.getPropertiesFromFile(operationPropertiesFilePath);
		int batchSize = new Integer(operationProperties.getProperty("batchSize"));
		SyncType syncType = null;
		switch (operationProperties.getProperty("syncType")) {
		case "NonIntrusiveOverwrite":
			syncType = SyncType.NON_INTRUSIVE_OVERWRITE;
			break;
		case "DeleteAllAndCopyFromScratch":
			syncType = SyncType.CLEAN_COPY;
			break;
		}
		String reportsFolderPath = operationProperties.getProperty("reportsFolderPath");
		String pidListFilePath = operationProperties.getProperty("pidListFilePath");
		String pidColorListFilePath = operationProperties.getProperty("pidColorListFilePath");
		SolrService service = standaloneApplicationContext.getBean(SolrServiceImpl.class);
//		SolrService service = standaloneApplicationContext.getBean(MultiThreadedSolrServiceImpl.class);
		switch(operation) {
			case "fullSync": 
				System.out.println("Received synchronization request from command the line");
				service.importToSolrFromDataBase(batchSize, solrServerUrl, syncType, null, null);
				break;
			case "report": 
				System.out.println("Received difference report generation request from the command line");
				service.generateDifferenceReports(solrServerUrl, reportsFolderPath, batchSize);
				break;
			case "selectedPidsSync": 
				System.out.println("Received synchronization request from command line for selected Pids");
				service.addDocumentsToSolrForSelectedPidIdsFromFile(new Long(batchSize), pidListFilePath, solrServerUrl);
				break;
			case "selectedPidColorsSync": 
				System.out.println("Received synchronization request from command the line for selected PidColors");
				service.addDocumentsToSolrForSelectedPidColorIdsFromFile(new Long(batchSize), pidColorListFilePath, solrServerUrl);;
				break;
			default:
				System.out.println("Missing or Invalid operation argument. Please provide proper value for first program argument ('sync'/'report')");
				System.exit(-1);
		}
	}

	private static void setup(String dbConnectionUrl, String username, String password) throws SQLException {
		Dao dao = standaloneApplicationContext.getBean(SpringDaoImpl.class);
		DataSource ds = Util.getBasicDataSourceForOracleConnection(dbConnectionUrl, username, password);
		DataSourceTransactionManager tm = new DataSourceTransactionManager(ds);
		tm.setDataSource(ds);
		dao.setDataSource(ds);
	}
}
