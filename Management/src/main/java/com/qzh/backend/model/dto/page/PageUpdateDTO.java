package com.qzh.backend.model.dto.page;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Data
public class PageUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父页面ID，NULL 或 0 表示顶级
     */
    private Long parentId;

    /**
     * 页面/菜单显示名称
     */
    @NotNull(message = "页面名称不能为空")
    private String name;

    /**
     * 前端路由路径，如 /users
     */
    @NotNull(message = "页面路径不能为空")
    private String path;

    /**
     * 前端组件路径/名称（前端用于动态路由）
     */
    @NotNull(message = "页面组件不能为空")
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序值，越大越靠前
     */
    @NotNull(message = "页面排序值不能为空")
    private Integer orderNum;

    /**
     * 是否可见（菜单展示）:1=显示,0=隐藏
     */
    @NotNull(message = "页面状态不能为空")
    private Integer visible;

    /**
     * 扩展字段，存放额外信息（如权限提示、layout 等）
     */
    private String meta;
}

