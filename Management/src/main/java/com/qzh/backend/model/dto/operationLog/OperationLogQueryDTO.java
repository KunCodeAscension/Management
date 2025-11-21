package com.qzh.backend.model.dto.operationLog;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.OperationLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogQueryDTO extends PageRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志唯一标识
     */
    private Long id;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人IP地址
     */
    private String operatorIp;

    /**
     * 操作设备/浏览器信息
     */
    private String operatorDevice;

    /**
     * 操作所属系统模块
     */
    private String systemModule;

    /**
     * 操作详细内容
     */
    private String operationContent;

    /**
     * 操作结果
     */
    private String operationResult;

    /**
     * 错误信息（操作失败时记录）
     */
    private String errorMsg;

    /**
     * 操作开始时间（查询创建时间范围）
     */
    private Date startTime;

    /**
     * 操作结束时间（查询创建时间范围）
     */
    private Date endTime;

    public static QueryWrapper<OperationLog> getQueryWrapper(OperationLogQueryDTO dto) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            queryWrapper.orderByDesc("operationTime");
            return queryWrapper;
        }
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotNull(dto.getId()), "id", dto.getId());
        queryWrapper.eq(ObjUtil.isNotNull(dto.getOperatorId()), "operatorId", dto.getOperatorId());
        queryWrapper.like(StrUtil.isNotBlank(dto.getOperatorIp()), "operatorIp", dto.getOperatorIp());
        queryWrapper.like(StrUtil.isNotBlank(dto.getOperatorDevice()), "operatorDevice", dto.getOperatorDevice());
        queryWrapper.like(StrUtil.isNotBlank(dto.getSystemModule()), "systemModule", dto.getSystemModule());
        queryWrapper.like(StrUtil.isNotBlank(dto.getOperationContent()), "operationContent", dto.getOperationContent());
        queryWrapper.eq(StrUtil.isNotBlank(dto.getOperationResult()), "operationResult", dto.getOperationResult());
        queryWrapper.like(StrUtil.isNotBlank(dto.getErrorMsg()), "errorMsg", dto.getErrorMsg());
        queryWrapper.ge(ObjUtil.isNotNull(dto.getStartTime()), "operationTime", dto.getStartTime());
        queryWrapper.lt(ObjUtil.isNotNull(dto.getEndTime()), "operationTime", dto.getEndTime());
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

}
