package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_operation_log")
public class OperationLog implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 日志唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作执行时间
     */
    private Date operationTime;

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
}
