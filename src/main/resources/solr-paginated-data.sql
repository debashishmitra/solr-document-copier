SELECT t.* 
  FROM (
        SELECT ROWNUM AS rn
             , t.* 
          FROM (select pc.pid_color_id as "id",
						pc.nrf_color as "pidColor",
						p.brand_code as "brandCode",
						p.vendor_id as "vendorId",
						p.department_id as "departmentId",
						p.pid as "pid",
						p.pid_id as "pidId",
						p.class_id as "classId",
						pcm.marketing_id as "marketingId",
						pt.product_Type_name as "productType",
						p.product_type_id as "productTypeId",
						p.pid_description as "pidDescription",
						p.place_holder_flag as "isPlaceHolder",
						p.active_flag as "activeFlag",
						pc.MERCH_APPROVED_FLAG as "isMerchApproved",
						p.PLACE_HOLDER_FLAG as "isPIDPlaceHolder",
						pc.PLACE_HOLDER_FLAG as "isPIDColorPlaceHolder",
						p.ACTIVE_FLAG as "isPIDActiveFlag",
						p.DIVISION_ID as "divisionId",
						p.MASTER_STYLE_ID as "masterStyleId",
						pc.SUBCLASS_ID as "subclassId"
						from PRT_PID p, PRT_PID_COLOR pc, OCP1PRTM.PRT_PID_COLOR_MKTG_ASSOC pcm, OCP1PRTM.PRT_PRODUCT_TYPE pt
						where p.pid_id = pc.pid_id
						and pcm.pid_color_id = pc.pid_color_id
						and p.PRODUCT_TYPE_ID = pt.PRODUCT_TYPE_ID
						order by pc.PID_COLOR_ID ASC) t
         WHERE ROWNUM <= :P_LAST_ROW
       ) t
 WHERE rn >= :P_FIRST_ROW