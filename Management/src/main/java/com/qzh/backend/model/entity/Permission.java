package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 权限资源表
 * 对应表：sys_permission
 */
@Data
@TableName("sys_permission")
public class Permission implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID（主键，自增）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 权限名称（唯一）
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 创建时间（默认当前时间）
     */
    private Date createTime;

    /**
     * 更新时间（默认当前时间，更新时自动刷新）
     */
    private Date updateTime;

    /**
     * 创建人ID
     */
    private Long createBy;
}