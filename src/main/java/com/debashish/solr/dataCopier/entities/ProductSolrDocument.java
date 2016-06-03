/**
 * 
 */
package com.debashish.solr.dataCopier.entities;

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @author Debashish Mitra
 *
 */
public class ProductSolrDocument {

	@Field
	private String id;

	@Field
	private Long pidColor;

	@Field
	private String nrfColorDesc;
	
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

//	@Field
//	private Boolean isPlaceHolder;

	@Field
	private Boolean activeFlag;

	@Field
	private Boolean isMerchApproved;

	@Field
	private Boolean isPIDPlaceHolder;

	@Field
	private Boolean isPIDColorPlaceHolder;

	@Field
	private Boolean isPIDActiveFlag;

	@Field
	private String pidChannel;

	@Field
	private String pidColorChannel;

	@Field
	private Long masterStyleId;

	@Field
	private Long subclassId;

	@Field
	private Long divisionId;
	
//	@Field("last_modified")
//	private String lastModified;

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

	public String getNrfColorDesc() {
		return nrfColorDesc;
	}

	public void setNrfColorDesc(String nrfColorDesc) {
		this.nrfColorDesc = nrfColorDesc;
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

//	public Boolean getIsPlaceHolder() {
//		return isPlaceHolder;
//	}
//
//	public void setIsPlaceHolder(Boolean isPlaceHolder) {
//		this.isPlaceHolder = isPlaceHolder;
//	}

	public Boolean getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Boolean getIsMerchApproved() {
		return isMerchApproved;
	}

	public void setIsMerchApproved(Boolean isMerchApproved) {
		this.isMerchApproved = isMerchApproved;
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

	public Boolean getIsPIDActiveFlag() {
		return isPIDActiveFlag;
	}

	public void setIsPIDActiveFlag(Boolean isPIDActiveFlag) {
		this.isPIDActiveFlag = isPIDActiveFlag;
	}

	public String getPidChannel() {
		return pidChannel;
	}

	public void setPidChannel(String pidChannel) {
		this.pidChannel = pidChannel;
	}

	public String getPidColorChannel() {
		return pidColorChannel;
	}

	public void setPidColorChannel(String pidColorChannel) {
		this.pidColorChannel = pidColorChannel;
	}

	public Long getMasterStyleId() {
		return masterStyleId;
	}

	public void setMasterStyleId(Long masterStyleId) {
		this.masterStyleId = masterStyleId;
	}

	public Long getSubclassId() {
		return subclassId;
	}

	public void setSubclassId(Long subclassId) {
		this.subclassId = subclassId;
	}

	public Long getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(Long divisionId) {
		this.divisionId = divisionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeFlag == null) ? 0 : activeFlag.hashCode());
		result = prime * result + ((brandCode == null) ? 0 : brandCode.hashCode());
		result = prime * result + ((classId == null) ? 0 : classId.hashCode());
		result = prime * result + ((departmentId == null) ? 0 : departmentId.hashCode());
		result = prime * result + ((divisionId == null) ? 0 : divisionId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((isMerchApproved == null) ? 0 : isMerchApproved.hashCode());
		result = prime * result + ((isPIDActiveFlag == null) ? 0 : isPIDActiveFlag.hashCode());
		result = prime * result + ((isPIDColorPlaceHolder == null) ? 0 : isPIDColorPlaceHolder.hashCode());
		result = prime * result + ((isPIDPlaceHolder == null) ? 0 : isPIDPlaceHolder.hashCode());
//		result = prime * result + ((isPlaceHolder == null) ? 0 : isPlaceHolder.hashCode());
		result = prime * result + ((masterStyleId == null) ? 0 : masterStyleId.hashCode());
		result = prime * result + ((nrfColorDesc == null) ? 0 : nrfColorDesc.hashCode());
		result = prime * result + ((pid == null) ? 0 : pid.hashCode());
		result = prime * result + ((pidChannel == null) ? 0 : pidChannel.hashCode());
		result = prime * result + ((pidColor == null) ? 0 : pidColor.hashCode());
		result = prime * result + ((pidColorChannel == null) ? 0 : pidColorChannel.hashCode());
		result = prime * result + ((pidDescription == null) ? 0 : pidDescription.hashCode());
		result = prime * result + ((pidId == null) ? 0 : pidId.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((productTypeId == null) ? 0 : productTypeId.hashCode());
		result = prime * result + ((subclassId == null) ? 0 : subclassId.hashCode());
		result = prime * result + ((vendorId == null) ? 0 : vendorId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductSolrDocument other = (ProductSolrDocument) obj;
		if (activeFlag == null) {
			if (other.activeFlag != null)
				return false;
		} else if (!activeFlag.equals(other.activeFlag))
			return false;
		if (brandCode == null) {
			if (other.brandCode != null)
				return false;
		} else if (!brandCode.equals(other.brandCode))
			return false;
		if (classId == null) {
			if (other.classId != null)
				return false;
		} else if (!classId.equals(other.classId))
			return false;
		if (departmentId == null) {
			if (other.departmentId != null)
				return false;
		} else if (!departmentId.equals(other.departmentId))
			return false;
		if (divisionId == null) {
			if (other.divisionId != null)
				return false;
		} else if (!divisionId.equals(other.divisionId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isMerchApproved == null) {
			if (other.isMerchApproved != null)
				return false;
		} else if (!isMerchApproved.equals(other.isMerchApproved))
			return false;
		if (isPIDActiveFlag == null) {
			if (other.isPIDActiveFlag != null)
				return false;
		} else if (!isPIDActiveFlag.equals(other.isPIDActiveFlag))
			return false;
		if (isPIDColorPlaceHolder == null) {
			if (other.isPIDColorPlaceHolder != null)
				return false;
		} else if (!isPIDColorPlaceHolder.equals(other.isPIDColorPlaceHolder))
			return false;
		if (isPIDPlaceHolder == null) {
			if (other.isPIDPlaceHolder != null)
				return false;
		} else if (!isPIDPlaceHolder.equals(other.isPIDPlaceHolder))
			return false;
//		if (isPlaceHolder == null) {
//			if (other.isPlaceHolder != null)
//				return false;
//		} else if (!isPlaceHolder.equals(other.isPlaceHolder))
//			return false;
		if (masterStyleId == null) {
			if (other.masterStyleId != null)
				return false;
		} else if (!masterStyleId.equals(other.masterStyleId))
			return false;
		if (nrfColorDesc == null) {
			if (other.nrfColorDesc != null)
				return false;
		} else if (!nrfColorDesc.equals(other.nrfColorDesc))
			return false;
		if (pid == null) {
			if (other.pid != null)
				return false;
		} else if (!pid.equals(other.pid))
			return false;
		if (pidChannel == null) {
			if (other.pidChannel != null)
				return false;
		} else if (!pidChannel.equals(other.pidChannel))
			return false;
		if (pidColor == null) {
			if (other.pidColor != null)
				return false;
		} else if (!pidColor.equals(other.pidColor))
			return false;
		if (pidColorChannel == null) {
			if (other.pidColorChannel != null)
				return false;
		} else if (!pidColorChannel.equals(other.pidColorChannel))
			return false;
		if (pidDescription == null) {
			if (other.pidDescription != null)
				return false;
		} else if (!pidDescription.equals(other.pidDescription))
			return false;
		if (pidId == null) {
			if (other.pidId != null)
				return false;
		} else if (!pidId.equals(other.pidId))
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		if (productTypeId == null) {
			if (other.productTypeId != null)
				return false;
		} else if (!productTypeId.equals(other.productTypeId))
			return false;
		if (subclassId == null) {
			if (other.subclassId != null)
				return false;
		} else if (!subclassId.equals(other.subclassId))
			return false;
		if (vendorId == null) {
			if (other.vendorId != null)
				return false;
		} else if (!vendorId.equals(other.vendorId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProductSolrDocument [id=" + id + ", pidColor=" + pidColor + ", nrfColorDesc=" + nrfColorDesc + ", brandCode=" + brandCode + ", vendorId=" + vendorId + ", departmentId=" + departmentId + ", pid=" + pid + ", pidId=" + pidId + ", classId=" + classId + ", marketingId=" + marketingId + ", productType=" + productType + ", productTypeId=" + productTypeId + ", pidDescription=" + pidDescription + ", activeFlag=" + activeFlag + ", isMerchApproved=" + isMerchApproved + ", isPIDPlaceHolder=" + isPIDPlaceHolder + ", isPIDColorPlaceHolder=" + isPIDColorPlaceHolder + ", isPIDActiveFlag=" + isPIDActiveFlag + ", pidChannel=" + pidChannel + ", pidColorChannel=" + pidColorChannel + ", masterStyleId=" + masterStyleId + ", subclassId=" + subclassId + ", divisionId=" + divisionId + "]";
	}
}
