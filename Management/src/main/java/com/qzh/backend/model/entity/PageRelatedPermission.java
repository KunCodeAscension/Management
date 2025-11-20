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
 * 页面按钮/动作与权限表
 * 对应表：sys_page_permission
 */
@Data
@TableName("sys_page_permission")
public class PageRelatedPermission implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（自增）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属页面ID
     */
    private Long pageId;

    /**
     * 权限ID（sys_permission.id）
     */
    private Long permissionId;

    /**
     * 动作标识，如 create/update/delete/export
     */
    private String action;

    /**
     * 描述
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

