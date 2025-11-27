package com.qzh.backend.model.dto.product;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.InventoryDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class InventoryDetailQueryDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单ID (关联的采购、销售等订单ID)
     */
    private Long orderId;

    /**
     * 变动类型 (0-出库，1-入库)
     */
    private Integer type;

    /**
     * 订单类型 (0-采购，1-采退，2-销售，3-销退)
     */
    private Integer orderType;

    /**
     * 开始时间 (创建时间 >= startTime)
     */
    private Date startTime;

    /**
     * 结束时间 (创建时间 <= endTime)
     */
    private Date endTime;

    public static QueryWrapper<InventoryDetail> getQueryWrapper(InventoryDetailQueryDTO dto) {
        QueryWrapper<InventoryDetail> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        Long productId = dto.getProductId();
        Long orderId = dto.getOrderId();
        Integer type = dto.getType();
        Integer orderType = dto.getOrderType();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.eq(ObjectUtil.isNotNull(productId), "productId", productId);
        queryWrapper.eq(ObjectUtil.isNotNull(orderId), "orderId", orderId);
        queryWrapper.eq(ObjectUtil.isNotNull(orderType), "orderType", orderType);
        queryWrapper.eq(ObjectUtil.isNotNull(type), "type", type);
        queryWrapper.ge(ObjectUtil.isNotNull(startTime), "createTime", startTime);
        queryWrapper.le(ObjectUtil.isNotNull(endTime), "createTime", endTime);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}