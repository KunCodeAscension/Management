package com.qzh.backend.model.dto.product;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.PurchaseOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class PurchaseOrderQueryDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 门店ID */
    private Long storeId;

    /** 供应商ID */
    private Long supplierId;

    /** 订单状态（0-待发货，1-已发货，2-已入库） */
    private Integer status;

    /** 下单时间-开始 */
    private Date startTime;

    /** 下单时间-结束 */
    private Date endTime;

    public static QueryWrapper<PurchaseOrder> getQueryWrapper(PurchaseOrderQueryDTO  dto) {
        QueryWrapper<PurchaseOrder> queryWrapper = new QueryWrapper<>();
        if(dto == null){
            return queryWrapper;
        }
        Long productId = dto.getProductId();
        String productName = dto.getProductName();
        Long storeId = dto.getStoreId();
        Long supplierId = dto.getSupplierId();
        Integer status = dto.getStatus();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.eq(ObjectUtil.isNotNull(productId),"productId", productId);
        queryWrapper.like(ObjectUtil.isNotNull(productName),"productName", productName);
        queryWrapper.eq(ObjectUtil.isNotNull(storeId),"storeId", storeId);
        queryWrapper.eq(ObjectUtil.isNotNull(supplierId),"supplierId", supplierId);
        queryWrapper.eq(ObjectUtil.isNotNull(status),"status", status);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}