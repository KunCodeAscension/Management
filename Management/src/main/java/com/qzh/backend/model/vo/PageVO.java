package com.qzh.backend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.qzh.backend.model.entity.PageInfo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer orderNum;

    private Integer visible;

    private String meta;

    private Date createTime;

    private Date updateTime;

    private Long createBy;

    /**
     * 页面下绑定的接口权限列表
     */
    private List<PermissionVO> permissions;

    /**
     * 子页面列表（用于树形结构）
     */
    private List<PageVO> children;

    public static PageVO toPageVO(PageInfo pageInfo) {
        if (pageInfo == null) {
            return null;
        }
        return BeanUtil.copyProperties(pageInfo, PageVO.class);
    }

    public static List<PageVO> toPageVOList(List<PageInfo> pageInfos) {
        if (CollectionUtil.isEmpty(pageInfos)) {
            return null;
        }
        return BeanUtil.copyToList(pageInfos, PageVO.class);
    }
}

