package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.page.PageCreateDTO;
import com.qzh.backend.model.dto.page.PageEditPermissionDTO;
import com.qzh.backend.model.dto.page.PageQueryDTO;
import com.qzh.backend.model.dto.page.PageUpdateDTO;
import com.qzh.backend.model.entity.PageInfo;
import com.qzh.backend.model.vo.PageVO;
import com.qzh.backend.model.vo.PermissionVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface PageService extends IService<PageInfo> {

    /**
     * 分页查询页面列表
     */
    Page<PageVO> getPageList(PageQueryDTO queryDTO);

    /**
     * 根据ID查询页面详情
     */
    PageVO getPageDetailById(Long id);

    /**
     * 创建页面
     */
    Long createPage(PageCreateDTO createDTO,HttpServletRequest request);

    /**
     * 更新页面信息
     */
    Boolean updatePage(Long id, PageUpdateDTO updateDTO);

    /**
     * 删除页面
     */
    Boolean deletePage(Long id);

    /**
     * 获取所有页面
     */
    List<PageVO> getAllPageWithPermissions();

    /**
     * 页面-权限新增接口
     */
    Boolean addPagePermission(Long pageId, Long permissionId,HttpServletRequest request);

    /**
     * 页面-权限修改接口（覆盖式更新）
     */
    Boolean updatePagePermissions(Long pageId, PageEditPermissionDTO permissionDTO,HttpServletRequest request);

    List<PageInfo> getUserPage(HttpServletRequest request);
}

