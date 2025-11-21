package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.page.PageCreateDTO;
import com.qzh.backend.model.dto.page.PageEditPermissionDTO;
import com.qzh.backend.model.dto.page.PageQueryDTO;
import com.qzh.backend.model.dto.page.PageUpdateDTO;
import com.qzh.backend.model.entity.PageInfo;
import com.qzh.backend.model.vo.PageVO;
import com.qzh.backend.service.PageService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.qzh.backend.constants.Interface.PageInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.PAGE_MODULE;

@RestController
@RequestMapping("page")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    /**
     * 分页查询页面列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<PageVO>> getPageList(PageQueryDTO dto) {
        Page<PageVO> pagePage = pageService.getPageList(dto);
        return ResultUtils.success(pagePage);
    }

    /**
     * 根据ID查询页面详情
     */
    @GetMapping("/{id}")
    public BaseResponse<PageVO> getPageById(@PathVariable Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        PageVO pageVO = pageService.getPageDetailById(id);
        return ResultUtils.success(pageVO);
    }

    /**
     * 创建页面
     */
    @PostMapping
    @LogInfoRecord(SystemModule = PAGE_MODULE + ":" + PAGE_CREATE_POST)
    public BaseResponse<Long> createPage(@Valid @RequestBody PageCreateDTO dto,HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Long pageId = pageService.createPage(dto,request);
        return ResultUtils.success(pageId);
    }

    /**
     * 更新页面
     */
    @PutMapping("/{id}")
    @LogInfoRecord(SystemModule = PAGE_MODULE + ":" + PAGE_UPDATE_PUT)
    public BaseResponse<Void> updatePage(@PathVariable Long id, @Valid @RequestBody PageUpdateDTO dto) {
        Boolean result = pageService.updatePage(id, dto);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "页面更新出错");
        return ResultUtils.success(null);
    }

    /**
     * 删除页面
     */
    @DeleteMapping("/{id}")
    @LogInfoRecord(SystemModule = PAGE_MODULE + ":" + PAGE_DELETE_DELETE)
    public BaseResponse<Void> deletePage(@PathVariable Long id) {
        Boolean result = pageService.deletePage(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "页面删除出错");
        return ResultUtils.success(null);
    }

    /**
     * 返回所有页面数据
     */
    @GetMapping("all")
    public BaseResponse<List<PageVO>> getAllPage() {
        return ResultUtils.success(pageService.getAllPageWithPermissions());
    }

    /**
     * 给页面赋权
     */
    @PostMapping("/{pageId}/permission/{permissionId}")
    @LogInfoRecord(SystemModule = PAGE_MODULE + ":" + PAGE_ASSIGN_SINGLE_PERMISSION_POST)
    public BaseResponse<Void> addPagePermission(@PathVariable Long pageId, @PathVariable Long permissionId,HttpServletRequest request) {
        boolean success = pageService.addPagePermission(pageId, permissionId,request);
        ThrowUtils.throwIf(!success, ErrorCode.SYSTEM_ERROR, "页面权限新增失败");
        return ResultUtils.success(null);
    }

    /**
     * 页面权限批量编辑
     */
    @PutMapping("/{pageId}/permission")
    @LogInfoRecord(SystemModule = PAGE_MODULE + ":" + PAGE_ASSIGN_BATCH_PERMISSION_PUT)
    public BaseResponse<Void> updatePagePermissions(@PathVariable Long pageId, @RequestBody PageEditPermissionDTO permissionDTO,HttpServletRequest request) {
        boolean success = pageService.updatePagePermissions(pageId, permissionDTO,request);
        ThrowUtils.throwIf(!success, ErrorCode.SYSTEM_ERROR, "页面权限修改失败");
        return ResultUtils.success(null);
    }

    @PostMapping("user")
    public BaseResponse<List<PageInfo>> getUserPage(HttpServletRequest request) {
        List<PageInfo> userPage = pageService.getUserPage(request);
        return ResultUtils.success(userPage);
    }
}

