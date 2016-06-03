/**
 * 
 */
package com.debashish.solr.dataCopier.dao.rowMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.debashish.solr.dataCopier.entities.ProductSolrDocument;

/**
 * @author Debashish Mitra
 *
 */
public class SolrDocumentRowMapper implements RowMapper<ProductSolrDocument> {

	@Override
	public ProductSolrDocument mapRow(ResultSet rs, int rowNumber) throws SQLException {
		
		ProductSolrDocument productSolrDocument = new ProductSolrDocument();
		
		productSolrDocument.setId(rs.getString("id"));
		
		Long pidColor = rs.getLong("pidColor");
		if(!rs.wasNull()) {
			productSolrDocument.setPidColor(pidColor);
		}
		
		productSolrDocument.setNrfColorDesc(rs.getString("nrfColorDesc"));
		productSolrDocument.setBrandCode(rs.getString("brandCode"));
		
		Long vendorId = rs.getLong("vendorId");
		if(!rs.wasNull()){
			productSolrDocument.setVendorId(vendorId);
		}
		
		Long departmentId = rs.getLong("departmentId");
		if(!rs.wasNull()){
			productSolrDocument.setDepartmentId(departmentId);
		}
		
		productSolrDocument.setPid(rs.getString("pid"));
		productSolrDocument.setPidId(rs.getString("pidId"));
		
		Long classId = rs.getLong("classId");
		if(!rs.wasNull()){
			productSolrDocument.setClassId(classId);
		}
		
//		productSolrDocument.setProductType(rs.getString("productType"));
		
		Long productTypeId = rs.getLong("productTypeId");
		if(!rs.wasNull()){
			productSolrDocument.setProductTypeId(productTypeId);
		}
		
		productSolrDocument.setPidDescription(rs.getString("pidDescription"));
		productSolrDocument.setActiveFlag("Y".equalsIgnoreCase(rs.getString("activeFlag")) ? true : false);
//		productSolrDocument.setIsPlaceHolder("Y".equalsIgnoreCase(rs.getString("isPlaceHolder")) ? true : false);
		productSolrDocument.setIsMerchApproved("Y".equalsIgnoreCase(rs.getString("isMerchApproved")) ? true : false);
		productSolrDocument.setIsPIDPlaceHolder("Y".equalsIgnoreCase(rs.getString("isPIDPlaceHolder")) ? true : false);
		productSolrDocument.setIsPIDColorPlaceHolder("Y".equalsIgnoreCase(rs.getString("isPIDColorPlaceHolder")) ? true : false);
		productSolrDocument.setIsPIDActiveFlag("Y".equalsIgnoreCase(rs.getString("isPIDActiveFlag")) ? true : false);
		
		Long masterStyleId = rs.getLong("masterStyleId");
		if(!rs.wasNull()) {
			productSolrDocument.setMasterStyleId(masterStyleId);
		}
		
		Long subclassId = rs.getLong("subclassId");
		if(!rs.wasNull()){
			productSolrDocument.setSubclassId(subclassId);
		}
		
		Long divisionId = rs.getLong("divisionId");
		if(!rs.wasNull()){
			productSolrDocument.setDivisionId(divisionId);
		}
//		s.setLastModified("NOW-5HOURS");
		return productSolrDocument;
	}
}
