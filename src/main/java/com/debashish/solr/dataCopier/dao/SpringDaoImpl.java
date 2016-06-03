package com.debashish.solr.dataCopier.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.debashish.solr.dataCopier.concurrent.task.MarketingIdAssociationDataFetcherTask;
import com.debashish.solr.dataCopier.dao.rowMappers.PidIdRowMapper;
import com.debashish.solr.dataCopier.dao.rowMappers.SolrDocumentRowMapper;
import com.debashish.solr.dataCopier.entities.ProductSolrDocument;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateInterval;
import com.debashish.solr.dataCopier.rest.requestObjects.UpdateIntervalUnit;
import com.debashish.solr.dataCopier.util.Util;
import com.google.common.collect.Lists;

import oracle.sql.RAW;

/**
 * @author Debashish Mitra
 *
 */
@Repository("springDaoImpl")
public class SpringDaoImpl implements Dao {

	private JdbcTemplate jdbcTemplate;

	private Map<String, String> sqls;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<ProductSolrDocument> getProductSolrCollection1DataFromDatabaseForTheSpecifiedRange(Long firstRow, Long lastRow) {
		String sql = sqls.get("getProductSolrCollection1DataFromDatabaseInBatches");
		Object[] args = new Object[] { lastRow, firstRow };
		int[] argTypes = { java.sql.Types.INTEGER, java.sql.Types.INTEGER };
		double b =System.currentTimeMillis();
		List<ProductSolrDocument> solrDocuments = jdbcTemplate.query(sql, args, argTypes, new SolrDocumentRowMapper());
		double a =System.currentTimeMillis();
		System.out.println("Time taken to get records " + firstRow + " to " + lastRow + " = " + (a-b)/1000);
		return solrDocuments;
	}

	@Override
	public List<ProductSolrDocument> getProductSolrCollection1DataFromDatabaseForTheSpecifiedRangeAndElapsedTime(UpdateInterval updateInterval, Long firstRow, Long lastRow) {
		UpdateIntervalUnit timeUnit = updateInterval.getUpdateIntervalUnit();
		Integer timeValue = updateInterval.getValue();
		String sql = sqls.get("getProductSolrCollection1DataFromDatabaseInBatchesForTheSpecifiedElapsedTime");
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("$IntervalUnit", timeUnit.name());
		parameterMap.put("$IntervalValue", timeValue.toString());
		sql = Util.getSqlStringFromParameterizedSqlWithMultipleParameters(sql, parameterMap);
		Object[] args = new Object[] { lastRow, firstRow };
		int[] argTypes = { java.sql.Types.INTEGER, java.sql.Types.INTEGER };
		double b =System.currentTimeMillis();
		List<ProductSolrDocument> solrDocuments = jdbcTemplate.query(sql, args, argTypes, new SolrDocumentRowMapper());
		double a =System.currentTimeMillis();
		System.out.println("Time taken to get records " + firstRow + " to " + lastRow + " = " + (a-b)/1000);
		return solrDocuments;
	}
	
	@Override
	public List<ProductSolrDocument> getProductSolrCollection1DataForSelectedPidIds(List<String> pidList) {
		String sql = sqls.get("getProductSolrCollection1DataForSelectedPidIds");
		String sqlInClauseList = Util.getSqlInClauseListFromStringList(pidList);
		sql = sql.replace("pidIDList", sqlInClauseList);
		List<ProductSolrDocument> solrDocuments = jdbcTemplate.query(sql, new SolrDocumentRowMapper());
		return solrDocuments;
	}

	@Override
	public List<ProductSolrDocument> getProductSolrCollection1DataForSelectedPidColorIds(List<String> pidColorIdList) {
		String sql = sqls.get("getProductSolrCollection1DataForSelectedPidColorIds");
		String sqlInClauseList = Util.getSqlInClauseListFromStringList(pidColorIdList);
		sql = sql.replace("pidColorIDList", sqlInClauseList);
		List<ProductSolrDocument> solrDocuments = jdbcTemplate.query(sql, new SolrDocumentRowMapper());
		return solrDocuments;
	}

	@Override
	public Long getPidRowCount() {
		Long rowCount = this.jdbcTemplate.queryForObject("select count(*) from PRT_PID where UPPER(prt_pid.brand_code) in ('MCY', 'BLM')", Long.class);
		return rowCount;
	}

	@Override
	public Long getPidColorRowCount() {
		Long rowCount = this.jdbcTemplate.queryForObject("select count (*) from (select pid_color_id from prt_pid_color left outer join prt_pid on prt_pid_color.pid_id = prt_pid.pid_id where UPPER(prt_pid.brand_code) in ('MCY','BLM'))", Long.class);
		return rowCount;
	}

	@Override
	public Long getRowCount(String sql) {
		Long rowCount = this.jdbcTemplate.queryForObject("select count (*) from (" + sql + ")", Long.class);
		return rowCount;
	}

	@Override
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapConcurrently() {
		System.out.println("Starting to make marketingId map");
		
		Map<String, List<Long>> pidColorIdToMarketingIdListMap = new HashMap<String, List<Long>>();
		double before = System.currentTimeMillis();
		List<Map<String, Object>> pidColorMarketingIdAssociationData = getPidColorMarketingIdAssociationDataConcurrently();

		for (Map<String, Object> row : pidColorMarketingIdAssociationData) {
			if (row.get("ID") != null && row.get("MARKETING_ID") != null) {
				Long marketingId = ((BigDecimal) row.get("MARKETING_ID")).longValue();
				String pidColorId = new RAW((byte[]) row.get("ID")).stringValue();
				List<Long> marketingIdList;
				if (pidColorIdToMarketingIdListMap.containsKey(pidColorId)) {
					marketingIdList = pidColorIdToMarketingIdListMap.get(pidColorId);
				} else {
					marketingIdList = new ArrayList<Long>();
					pidColorIdToMarketingIdListMap.put(pidColorId, marketingIdList);
				}
				marketingIdList.add(marketingId);
			}
		}
		double after = System.currentTimeMillis();
		System.out.println("Time taken to make marketingId map = " + (after - before)/1000);
		return pidColorIdToMarketingIdListMap;
	}
	
	/**
	 * 
	 * Returns a Map where the key is pid_color_id and value is the List of
	 * marketing IDs associated to it
	 */
	@Override
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMap() {
		System.out.println("Starting to make marketingId map");
		
		Map<String, List<Long>> pidColorIdToMarketingIdListMap = new HashMap<String, List<Long>>();
		double before = System.currentTimeMillis();
		List<Map<String, Object>> pidColorMarketingIdAssociationData = this.jdbcTemplate.queryForList("SELECT DISTINCT PID_COLOR_ID AS ID, MARKETING_ID FROM PRT_PID_COLOR_MKTG_ASSOC");

		for (Map<String, Object> row : pidColorMarketingIdAssociationData) {
			if (row.get("ID") != null && row.get("MARKETING_ID") != null) {
				Long marketingId = ((BigDecimal) row.get("MARKETING_ID")).longValue();
				String pidColorId = new RAW((byte[]) row.get("ID")).stringValue();
				List<Long> marketingIdList;
				if (pidColorIdToMarketingIdListMap.containsKey(pidColorId)) {
					marketingIdList = pidColorIdToMarketingIdListMap.get(pidColorId);
				} else {
					marketingIdList = new ArrayList<Long>();
					pidColorIdToMarketingIdListMap.put(pidColorId, marketingIdList);
				}
				marketingIdList.add(marketingId);
			}
		}
		double after = System.currentTimeMillis();
		System.out.println("Time taken to make marketingId map = " + (after - before)/1000);
		return pidColorIdToMarketingIdListMap;
	}

	private List<Map<String, Object>> getPidColorMarketingIdAssociationDataConcurrently() {
		ExecutorService es = Executors.newCachedThreadPool();
		AtomicInteger batchCounter= new AtomicInteger();
		AtomicBoolean hasMoreRecords = new AtomicBoolean(true);
		List<Future<List<Map<String, Object>>>> futures = new ArrayList<Future<List<Map<String, Object>>>>();
		for(int i=0; i<5; i++) {
			MarketingIdAssociationDataFetcherTask t = new MarketingIdAssociationDataFetcherTask(batchCounter, 30000, hasMoreRecords, this);
			Future<List<Map<String, Object>>> future = es.submit(t);
			futures.add(future);
		}
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>> ();
		for(Future<List<Map<String, Object>>> future : futures) {
			try {
				result.addAll(future.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@Override
	public List<Map<String, Object>> getPidColorMarketingIdAssociationDataForTheSpecifiedRange(long firstRow, long lastRow) {
		try {
			String parameterizedSql = "SELECT ID, MARKETING_ID from (SELECT DISTINCT PID_COLOR_ID AS ID, MARKETING_ID, ROWNUM as rn FROM PRT_PID_COLOR_MKTG_ASSOC order by PID_COLOR_ID asc) where rn between firstRow and lastRow";
			Map<String, String> parameterMap = new HashMap<String, String>();
			System.out.println("About to get records " + firstRow + " to " + lastRow);
			parameterMap.put("firstRow", String.valueOf(firstRow));
			parameterMap.put("lastRow", String.valueOf(lastRow));
			String sql = Util.getSqlStringFromParameterizedSqlWithMultipleParameters(parameterizedSql, parameterMap);
			List<Map<String, Object>> pidColorMarketingIdAssociationData = this.jdbcTemplate.queryForList(sql);
			return pidColorMarketingIdAssociationData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPids(List<String> pidIds) {
		Map<String, List<Long>> fullPidColorIdToMarketingIdListMap = new HashMap<String, List<Long>>();
		// Split up the list in to smaller lists so that there are not too mant
		// entries in the SQL IN Query
		List<List<String>> batches = Lists.partition(pidIds, 500);
		for (List<String> batch : batches) {
			Map<String, List<Long>> pidColorIdToMarketingIdListMap = new HashMap<String, List<Long>>();

			String inClauseString = Util.getSqlInClauseListFromStringList(batch);

			List<Map<String, Object>> rows = this.jdbcTemplate.queryForList("SELECT pcma.PID_COLOR_ID AS ID, pcma.MARKETING_ID FROM PRT_PID_COLOR_MKTG_ASSOC pcma left join prt_pid_color pc on pcma.pid_color_id=pc.pid_color_id where pc.pid_id in (" + inClauseString + ")");

			for (Map<String, Object> row : rows) {

				if (row.get("ID") != null && row.get("MARKETING_ID") != null) {
					Long marketingId = ((BigDecimal) row.get("MARKETING_ID")).longValue();
					String pidColorId = new RAW((byte[]) row.get("ID")).stringValue();
					List<Long> marketingIdList;
					if (pidColorIdToMarketingIdListMap.containsKey(pidColorId)) {
						marketingIdList = pidColorIdToMarketingIdListMap.get(pidColorId);
					} else {
						marketingIdList = new ArrayList<Long>();
						pidColorIdToMarketingIdListMap.put(pidColorId, marketingIdList);
					}
					marketingIdList.add(marketingId);
				}
			}
			fullPidColorIdToMarketingIdListMap.putAll(pidColorIdToMarketingIdListMap);
		}
		return fullPidColorIdToMarketingIdListMap;
	}

	@Override
	public Map<String, List<Long>> getPidColorIdToMarketingIdListMapForSelectedPidColors(List<String> pidColorIds) {
		Map<String, List<Long>> fullPidColorIdToMarketingIdListMap = new HashMap<String, List<Long>>();
		//Split up the list in to smaller lists so that there are not too mant entries in the SQL IN Query
		List<List<String>> batches = Lists.partition(pidColorIds, 500);
		for (List<String> batch : batches) {
			Map<String, List<Long>> pidColorIdToMarketingIdListMap = new HashMap<String, List<Long>>();

			String inClauseString = Util.getSqlInClauseListFromStringList(batch);

			List<Map<String, Object>> rows = this.jdbcTemplate.queryForList("SELECT pcma.PID_COLOR_ID AS ID, pcma.MARKETING_ID FROM PRT_PID_COLOR_MKTG_ASSOC pcma left join prt_pid_color pc on pcma.pid_color_id=pc.pid_color_id where pc.pid_color_id in (" + inClauseString + ")");

			for (Map<String, Object> row : rows) {
				if (row.get("ID") != null && row.get("MARKETING_ID") != null) {
					Long marketingId = ((BigDecimal) row.get("MARKETING_ID")).longValue();
					String pidColorId = new RAW((byte[]) row.get("ID")).stringValue();
					List<Long> marketingIdList;
					if (pidColorIdToMarketingIdListMap.containsKey(pidColorId)) {
						marketingIdList = pidColorIdToMarketingIdListMap.get(pidColorId);
					} else {
						marketingIdList = new ArrayList<Long>();
						pidColorIdToMarketingIdListMap.put(pidColorId, marketingIdList);
					}
					marketingIdList.add(marketingId);
				}
			}
			fullPidColorIdToMarketingIdListMap.putAll(pidColorIdToMarketingIdListMap);
		}
		return fullPidColorIdToMarketingIdListMap;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Map<String, String> getSqls() {
		return sqls;
	}

	public void setSqls(Map<String, String> sqls) {
		this.sqls = sqls;
	}

	@Override
	public List<ProductSolrDocument> getSelectedData(String sql) {
		List<ProductSolrDocument> solrDocuments = jdbcTemplate.query(sql, new SolrDocumentRowMapper());
		return solrDocuments;
	}

	@Override
	public Set<String> getFullPidIdSetFromDatabase() {
		String batchSql = "select pid_id from (select distinct(pid_id), ROWNUM rn from prt_pid_color order by pid_id asc) where rn between firstRecord and lastRecord";
		Set<String> pidIdFullSet = new HashSet<String>();
		int batch = 30000;
		int first=1;
		int last= batch;
		while(true) {
			String sql = Util.getSqlForTheGivenRange(batchSql, first, last);
			List<String> pidIdList = jdbcTemplate.query(sql, new PidIdRowMapper());
			if(pidIdList.size() == 0) {
				break;
			}
			pidIdFullSet.addAll(pidIdList);
			first+=batch;
			last+=batch;
		}
		return pidIdFullSet;
	}

	@Override
	public Set<String> getFullPidColorIdSetFromDatabase() {
		String batchSql = "select pid_color_id from (select pid_color_id , ROWNUM rn from prt_pid_color order by pid_color_id asc) where rn between firstRecord and lastRecord";
		Set<String> pidColorIdFullSet = new HashSet<String>();
		int batch = 30000;
		int first=1;
		int last= batch;
		double before = System.currentTimeMillis();
		while(true) {
			String sql = Util.getSqlForTheGivenRange(batchSql, first , last);
			
			List<String> pidColorIdList = jdbcTemplate.queryForList(sql, String.class);

			if(pidColorIdList.size() == 0) {
				break;
			}
			pidColorIdFullSet.addAll(pidColorIdList);
			first+=batch;
			last+=batch;
		}
		double after = System.currentTimeMillis();
		double timeTaken = (after-before)/1000;
		System.out.println("Time taken to get full PID Color ID set from the database = " + timeTaken + " seconds");
		return pidColorIdFullSet;
	}
	
	public Set<String> getPresentPidColorIdSet(List<String> pidColorIdList) {
		String parameterizedSql = "select pid_color_id from prt_pid_color where pid_color_id in (pidColorIdList)";
		Set<String> resultList = new HashSet<String>();
		List<List<String>> batches = Lists.partition(pidColorIdList, 300);
		for(List<String> pidColorIdBatch : batches) {
			String sql = Util.getSqlStringFromParameterizedSql(parameterizedSql, "pidColorIdList", Util.getSqlInClauseListFromStringList(pidColorIdBatch));
			List<String> pidColorsPresentInDB = jdbcTemplate.queryForList(sql, String.class);
			resultList.addAll(pidColorsPresentInDB);
		}
		return resultList;
	}
}