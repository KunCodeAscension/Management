package com.qzh.backend.tools.query;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.SaleOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.Serializable;
import java.util.Date;

@Data
public class SaleOrderQueryDTO implements Serializable {

    @ToolParam(required = false, description = "产品ID")
    private Long productId;

    @ToolParam(required = false, description = "起始时间")
    private Date startTime;

    @ToolParam(required = false, description = "截至时间")
    private Date endTime;

    public static QueryWrapper<SaleOrder> getQueryWrapper(SaleOrderQueryDTO dto) {
        QueryWrapper<SaleOrder> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        Long productId = dto.getProductId();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        queryWrapper.eq(ObjectUtil.isNotNull(productId), "productId", productId);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        return queryWrapper;
    }

}