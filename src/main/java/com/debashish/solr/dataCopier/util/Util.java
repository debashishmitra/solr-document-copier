package com.debashish.solr.dataCopier.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.springframework.util.CollectionUtils;

/**
 * This class contains various useful methods that have been used in this Solr Data Copier application
 * 
 * @author Debashish Mitra
 * 
 */
public class Util {

	public static String getTimeStringFromMilliseconds(long inputTimeInMilliseconds) {
		StringBuilder s = new StringBuilder();

		long MILLISECONDS_IN_AN_HOUR = 3600000;
		long MILLISECONDS_IN_A_MINUTE = 60000;
		long MILLISECONDS_IN_A_SECOND = 1000;

		long hours = inputTimeInMilliseconds / MILLISECONDS_IN_AN_HOUR;
		long minutes = (inputTimeInMilliseconds % MILLISECONDS_IN_AN_HOUR) / MILLISECONDS_IN_A_MINUTE;
		long seconds = ((inputTimeInMilliseconds % MILLISECONDS_IN_AN_HOUR) % MILLISECONDS_IN_A_MINUTE) / MILLISECONDS_IN_A_SECOND;
		long milliseconds = ((inputTimeInMilliseconds % MILLISECONDS_IN_AN_HOUR) % MILLISECONDS_IN_A_MINUTE) % MILLISECONDS_IN_A_SECOND;
		
		s.append(hours > 0 ? hours + " HOURS " : "").append(minutes > 0 ? minutes + " MINUTES " : "").append(seconds > 0 ?  seconds + " SECONDS ":"").append(milliseconds > 0 ? milliseconds + " MILLI SECONDS":"");
		return s.toString();
	}

	public static String getTimeStringFromMillisecondsAlternativeImplementation(long milliseconds) {
		StringBuilder s = new StringBuilder();
		long hours = milliseconds / 3600000;
		long minutes = (milliseconds - hours * 3600000) / 60000;
		long seconds = (milliseconds - (hours * 3600000) - (minutes * 60000)) / 1000;

		s.append(hours > 0 ? hours + " HOURS, " : "").append(minutes > 0 ? minutes + " MINUTES, " : "").append(seconds + " SECONDS");
		return s.toString();
	}

	public static long getMilliSeconds(long hour, long minute, long seconds) {
		return ((hour * 3600000) + (minute * 60000) + (seconds * 1000));
	}

	public static void main(String[] args) {
		System.out.println(getTimeStringFromMilliseconds(3661000));
		System.out.println(getMilliSeconds(1, 1, 1));
	}

	public static String getSqlInClauseListFromStringList(List<String> pids) {

		StringBuilder inQueryString = new StringBuilder();
		for (String pid : pids) {
			inQueryString.append("'").append(pid).append("',");
		}
		int positionOfLastPeriod = inQueryString.lastIndexOf(",");
		String s = inQueryString.replace(positionOfLastPeriod, positionOfLastPeriod + 1, "").toString();

		return s;
	}

	public static DataSource getBasicDataSourceForOracleConnection(String url, String username, String password) throws SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("oracle.jdbc.OracleDriver");
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(url);
		return ds;
	}

	public static String getHyphenatedForm(String input) {
		String output = new StringBuilder(input).insert(8, '-').insert(13, '-').insert(18, '-').insert(23, '-').toString();
		return output;
	}

	public static String getNonHyphenatedForm(String input) {
		String output = input.replace("-", "");
		return output;
	}

	public static List<String> getHyphenatedForm(List<String> inputStringList) {
		if (!CollectionUtils.isEmpty(inputStringList)) {
			List<String> result = new ArrayList<String>();
			for (String s : inputStringList) {
				String hyphenatedString = getHyphenatedForm(s);
				result.add(hyphenatedString);
			}
			return result;
		}
		return inputStringList;
	}

	public static List<String> getNonHyphenatedForm(List<String> inputStringList) {
		if (!CollectionUtils.isEmpty(inputStringList)) {
			List<String> result = new ArrayList<String>();
			for (String s : inputStringList) {
				String nonHyphenatedString = getNonHyphenatedForm(s);
				result.add(nonHyphenatedString);
			}
			return result;
		}
		return inputStringList;
	}

	public static String getSolrInQuery(String fieldName, List<String> list) {
		StringBuilder inQuery = new StringBuilder();
		inQuery.append(fieldName + ":(");
		for (String value : list) {
			inQuery.append(value).append(" ");
		}
		inQuery.append(")");
		return inQuery.toString();
	}

	public static String getSqlForTheGivenRange(String parameterizedSql, int firstRow, int lastRow) {
		StringBuilder sql = new StringBuilder(parameterizedSql);
		sql.replace(sql.indexOf("firstRecord"), sql.indexOf("firstRecord") + 11, String.valueOf(firstRow));
		sql.replace(sql.indexOf("lastRecord"), sql.indexOf("lastRecord") + 10, String.valueOf(lastRow));
		return sql.toString();
	}

	public static String getSqlStringFromParameterizedSqlWithMultipleParameters(String parameterizedSql, Map<String, String> parameterMap) {
		StringBuilder sb = new StringBuilder(parameterizedSql);
		for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
			String parameterName = entry.getKey();
			String parameterValue = entry.getValue();
			sb = new StringBuilder(getSqlStringFromParameterizedSql(sb.toString(), parameterName, parameterValue));
		}
		String result = sb.toString();
		return result;
	}

	public static String getSqlStringFromParameterizedSql(String parameterizedSql, String parameterName, String parameterValue) {
		StringBuilder sql = new StringBuilder(parameterizedSql);
		sql.replace(sql.indexOf(parameterName), sql.indexOf(parameterName) + parameterName.length(), String.valueOf(parameterValue));
		return sql.toString();
	}

	public static boolean nullSafeEquals(Object ob1, Object ob2) {
		return (ob1 == null) ? ob2 == null : ob1.equals(ob2);
	}

	public static <E> boolean nullSafeEqualsForLists(List<E> l1, List<E> l2) {
		if (l1 == l2) {
			return true;
		}
		if (l1 == null && null == l2) {
			return true;
		}
		if ((l1 == null && null != l2) || (l1 != null && null == l2)) {
			return false;
		}
		if (l1.size() != l2.size()) {
			return false;
		}
		for (int i = 0; i < l1.size(); i++) {
			Object ob1 = l1.get(i);
			Object ob2 = l2.get(i);
			if (!nullSafeEquals(ob1, ob2)) {
				return false;
			}
		}
		return true;
	}

	public static <E> void writeCollectionToFile(String fullFilePath, Collection<E> data, boolean append) {

		try {
			FileUtils.writeLines(new File(fullFilePath), data, append);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeStringToFile(String fullFilePath, String data, boolean append) {

		try {
			FileUtils.writeStringToFile(new File(fullFilePath), data, append);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeStringToExistingFile(File file, String data, boolean append) {
		try {
			FileUtils.writeStringToFile(file, data, append);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Properties getPropertiesFromFile(String propertiesFilePath) throws IOException {
		InputStream inputStream = null;
		Properties connectionProperties = null;
		try {
			inputStream = new FileInputStream(propertiesFilePath);
			connectionProperties = new Properties();
			connectionProperties.load(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return connectionProperties;
	}
}
