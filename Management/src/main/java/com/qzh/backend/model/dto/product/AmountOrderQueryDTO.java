package com.qzh.backend.model.dto.product;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.AmountOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class AmountOrderQueryDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 金额单类型 (0-采购，1-采退，2-销售，3-销退)
     */
    private Integer type;

    /**
     * 金额单状态 (0-待支付，1-已支付，2-已取消)
     */
    private Integer status;

    /**
     * 付款人ID（门店ID）
     */
    private Long payerId;

    /**
     * 收款人ID（供应商ID）
     */
    private Long payeeId;

    /**
     * 开始时间 (创建时间)
     */
    private Date startTime;

    /**
     * 结束时间 (创建时间)
     */
    private Date endTime;

    public static QueryWrapper<AmountOrder> getQueryWrapper(AmountOrderQueryDTO dto) {
        QueryWrapper<AmountOrder> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        Long storeId = dto.getStoreId();
        Long payerId = dto.getPayerId();
        Long payeeId = dto.getPayeeId();
        Integer type = dto.getType();
        Integer status = dto.getStatus();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.eq(ObjectUtil.isNotNull(storeId), "storeId", storeId);
        queryWrapper.eq(ObjectUtil.isNotNull(type), "type", type);
        queryWrapper.eq(ObjectUtil.isNotNull(status), "status", status);
        queryWrapper.eq(ObjectUtil.isNotNull(payerId), "payerId", payerId);
        queryWrapper.eq(ObjectUtil.isNotNull(payeeId), "payeeId", payeeId);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        return queryWrapper;
    }
}