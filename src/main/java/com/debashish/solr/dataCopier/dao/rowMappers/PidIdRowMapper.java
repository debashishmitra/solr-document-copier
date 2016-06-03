/**
 * 
 */
package com.debashish.solr.dataCopier.dao.rowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author Debashish Mitra
 *
 */
public class PidIdRowMapper implements RowMapper<String> {

	@Override
	public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
		String pidId = rs.getString("pid_id");
		return pidId;
	}

}
