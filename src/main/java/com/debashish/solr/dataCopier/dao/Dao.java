package com.debashish.solr.dataCopier.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.debashish.solr.dataCopier.entities.ProductSolrDocument;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;

/**
 * @author Debashish Mitra
 *
 */
public interface Dao {
	
	public List<ProductSolrDocument> getProductSolrCollection1DataFromDatabaseForTheSpecifiedRange(Long firstRow, Long lastRow);

	public List<ProductSolrDocument> getProductSolrCollection1DataFromDatabaseForTheSpecifiedRangeAndElapsedTime(UpdateInterval updateInterval, Long firstRow, Long lastRow);
	
	public Long getPidRowCount();

	public Long getPidColorRowCount();

	public Map<String, List<Long>> getPidColorIdToMarketingIdListMap();
	
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPids(List<String> pidIds);
	
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPidColors(List<String> pidColorIds);
	
	public List<ProductSolrDocument> getSelectedData(String sql);
	
	public Long getRowCount(String sql);
	
	public List<ProductSolrDocument> getProductSolrCollection1DataForSelectedPidColorIds(List<String> pidColorIdList);
	
	public List<ProductSolrDocument> getProductSolrCollection1DataForSelectedPidIds(List<String> pidIdList);
	
	public Set<String> getFullPidIdSetFromDatabase();
	
	public Set<String> getFullPidColorIdSetFromDatabase();
	
	public void setDataSource(DataSource dataSource);
	
	public Set<String> getPresentPidColorIdSet(List<String> pidColorIdList);

	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapConcurrently();

	public List<Map<String, Object>> getPidColorMarketingIdAssociationDataForTheSpecifiedRange(long firstRow, long lastRow);
}