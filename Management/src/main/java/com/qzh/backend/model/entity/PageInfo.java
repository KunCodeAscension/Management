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
 * 页面/菜单表
 * 对应表：sys_page
 */
@Data
@TableName("sys_page")
public class PageInfo implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 页面ID（主键，自增）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 父页面ID，NULL 或 0 表示顶级
     */
    private Long parentId;

    /**
     * 页面/菜单显示名称
     */
    private String name;

    /**
     * 前端路由路径，如 /users
     */
    private String path;

    /**
     * 前端组件路径/名称（前端用于动态路由）
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序值，越大越靠前
     */
    private Integer orderNum;

    /**
     * 是否可见（菜单展示）:1=显示,0=隐藏
     */
    private Integer visible;

    /**
     * 扩展字段，存放额外信息（如权限提示、layout 等）
     */
    private String meta;

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

