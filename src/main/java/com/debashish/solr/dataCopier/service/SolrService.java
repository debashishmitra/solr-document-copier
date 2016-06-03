package com.debashish.solr.dataCopier.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrServerException;

import com.debashish.solr.dataCopier.entities.ProductSolrDocument;
import com.debashish.solr.dataCopier.rest.requestObjects.SyncType;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateScope;
import com.debashish.solr.dataCopier.rest.responseObjects.SynchronizationResponse;

/**
 * @author Debashish Mitra
 *
 */
public interface SolrService {

    public void copyFrom3CoreSolrTo1CoreSolr(int batchSize, String sourceSolrServerUrl, String destinationSolrServerUrl);

    public void copyFrom1CoreSolrto1CoreSolr(String sourceSolrServerUrl, String destinationSolrServerUrl, int batchSize);

    public String importToSolrFromDataBase(int batchSize, String destinationSolrServerUrl, SyncType syncType, UpdateScope updateScope, UpdateInterval updateInterval);

    public void importOnlySelectedPidsToSolrFromDataBase(int batchSize, String destinationSolrServerUrl, List<String> pids);

    public int importOnlySelectedPidColorsToSolrFromDataBase(int batchSize, String destinationSolrServerUrl, List<String> pidColorIds);
    
    public Long getPidRowCount();

    public Long getPidColorRowCount();
    
    public void setDataSource(DataSource dataSource);

    public Map<String, List<Long>> getPidColorIdToMarketingIdListMap();

    public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPids(List<String> pidIds);

    public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPidColors(List<String> pidColorIds);
    
    public SynchronizationResponse synchronizeSolrWithDatabaseBasedOnIdComparisionOnly(String solrServerBaseUrl);

    public Set<String> getPidColorIdSetFromSolr(String solrServerBaseUrl);
    
    public List<ProductSolrDocument> getProductSolrCollection1DataFromSolrForSpecifiedPidColorIds(String solrServerBaseUrl, List<String> pidColorIds);

    public Set<String> getPidColorIdSetFromDatabase();

    public int verifyAndSafeDeleteSelectedPidColorsFromSolr(String destinationSolrServerBaseUrl, List<String> pidColorIds) throws SolrServerException, IOException;
    
    public void addDocumentsToSolr(String solrServerBaseUrl, List<ProductSolrDocument> documents);
    
    public SynchronizationResponse synchronizeSolrToDatabaseOnFullDataComparision(int batchSize, String solrServerBaseUrl);
    
    public void generateDifferenceReports(String solrServerBaseUrl, String reportFolderPath, int batchSize);
    
    public void addDocumentsToSolrForSelectedPidIdsFromFile(Long batchSize, String filePath, String solrServerBaseUrl) throws FileNotFoundException;

    public void addDocumentsToSolrForSelectedPidColorIdsFromFile(Long batchSize, String filePath, String solrServerBaseUrl) throws FileNotFoundException;

	public List<ProductSolrDocument> addMarketingAssociationInformation(List<ProductSolrDocument> solrDocuments, Map<String, List<Long>> pidColorIdToMarketingIdListMap);
    
    public int deleteExtraDocumentsFromSolr(String solrServerBaseUrl) throws SolrServerException, IOException;
    
    public int deleteSelectedPidsFromSolr(String destinationSolrServerUrl, List<String> pidIds) throws SolrServerException, IOException;
    
    public int deleteSelectedPidColorsFromSolr(String destinationSolrServerUrl, List<String> pidColorIds) throws SolrServerException, IOException;
}