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
 * 仓库实体类
 * 对应表名：sys_warehouse
 */
@Data
@TableName("sys_warehouse")
public class Warehouse implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 仓库唯一ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 仓库描述
     */
    private String description;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}