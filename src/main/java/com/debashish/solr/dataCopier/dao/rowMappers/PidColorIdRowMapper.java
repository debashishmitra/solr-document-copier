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
public class PidColorIdRowMapper implements RowMapper<String> {

	@Override
	public String mapRow(ResultSet rs, int rowNumber) throws SQLException {
		String pidColorId = rs.getString("pid_color_id");
		return pidColorId;
	}

}
