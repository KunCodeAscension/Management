package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 角色-页面关联表
 * 对应表：sys_role_page
 */
@Data
@TableName("sys_role_page")
public class RoleRelatedPage implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 页面ID
     */
    private Long pageId;

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

