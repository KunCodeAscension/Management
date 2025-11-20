package com.qzh.backend.model.dto.page;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageQueryDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父页面ID
     */
    private Long parentId;

    /**
     * 页面名称
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
     * 是否可见:1=显示,0=隐藏
     */
    private Integer visible;

    /**
     * 开始时间（用于查询创建时间范围）
     */
    private Date startTime;

    /**
     * 结束时间（用于查询创建时间范围）
     */
    private Date endTime;

    public static QueryWrapper<PageInfo> getQueryWrapper(PageQueryDTO dto) {
        QueryWrapper<PageInfo> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        Long parentId = dto.getParentId();
        String path = dto.getPath();
        String component = dto.getComponent();
        String name = dto.getName();
        Integer visible = dto.getVisible();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotNull(parentId), "parentId", parentId);
        queryWrapper.like(ObjUtil.isNotNull(name), "name", name);
        queryWrapper.like(ObjUtil.isNotNull(path), "path", path);
        queryWrapper.like(ObjUtil.isNotNull(component), "component", component);
        queryWrapper.eq(ObjUtil.isNotNull(visible), "visible", visible);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}

