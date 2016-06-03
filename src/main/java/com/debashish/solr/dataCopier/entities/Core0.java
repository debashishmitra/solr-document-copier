package com.debashish.solr.dataCopier.entities;

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Debashish Mitra
 *
 */
@Deprecated
public class Core0 {

	@Field
	private String id;

	@Field
	private Long pidColor;

	@Field
	private String brandCode;

	@Field
	private Long vendorId;

	@Field
	private Long departmentId;

	@Field
	private String pid;

	@Field
	private String pidId;

	@Field
	private Long classId;

	@Field
	private List<Long> marketingId;

	@Field
	private String productType;

	@Field
	private Long productTypeId;

	@Field
	private String pidDescription;

	@Field
	private Boolean isPlaceHolder;

	@Field
	private Boolean activeFlag;

	@Field
	private Boolean liveFlag;

	@Field
	private Boolean isPIDPlaceHolder;

	@Field
	private Boolean isPIDColorPlaceHolder;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getPidColor() {
		return pidColor;
	}

	public void setPidColor(Long pidColor) {
		this.pidColor = pidColor;
	}

	public String getBrandCode() {
		return brandCode;
	}

	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getPidId() {
		return pidId;
	}

	public void setPidId(String pidId) {
		this.pidId = pidId;
	}

	public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}

	public List<Long> getMarketingId() {
		return marketingId;
	}

	public void setMarketingId(List<Long> marketingId) {
		this.marketingId = marketingId;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public Long getProductTypeId() {
		return productTypeId;
	}

	public void setProductTypeId(Long productTypeId) {
		this.productTypeId = productTypeId;
	}

	public String getPidDescription() {
		return pidDescription;
	}

	public void setPidDescription(String pidDescription) {
		this.pidDescription = pidDescription;
	}

	public Boolean getIsPlaceHolder() {
		return isPlaceHolder;
	}

	public void setIsPlaceHolder(Boolean isPlaceHolder) {
		this.isPlaceHolder = isPlaceHolder;
	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Boolean getLiveFlag() {
		return liveFlag;
	}

	public void setLiveFlag(Boolean liveFlag) {
		this.liveFlag = liveFlag;
	}

	public Boolean getIsPIDPlaceHolder() {
		return isPIDPlaceHolder;
	}

	public void setIsPIDPlaceHolder(Boolean isPIDPlaceHolder) {
		this.isPIDPlaceHolder = isPIDPlaceHolder;
	}

	public Boolean getIsPIDColorPlaceHolder() {
		return isPIDColorPlaceHolder;
	}

	public void setIsPIDColorPlaceHolder(Boolean isPIDColorPlaceHolder) {
		this.isPIDColorPlaceHolder = isPIDColorPlaceHolder;
	}

}
