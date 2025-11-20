package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.page.PageCreateDTO;
import com.qzh.backend.model.dto.page.PageQueryDTO;
import com.qzh.backend.model.dto.page.PageUpdateDTO;
import com.qzh.backend.model.vo.PageVO;
import com.qzh.backend.service.PageService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/system/page")
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
     * 获取树形结构的页面列表
     */
    @GetMapping("/tree")
    public BaseResponse<List<PageVO>> getPageTree(@RequestParam(required = false) Long parentId) {
        List<PageVO> pageTree = pageService.getPageTree(parentId);
        return ResultUtils.success(pageTree);
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
    public BaseResponse<Long> createPage(@Valid @RequestBody PageCreateDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Long pageId = pageService.createPage(dto);
        return ResultUtils.success(pageId);
    }

    /**
     * 更新页面
     */
    @PutMapping("/{id}")
    public BaseResponse<Void> updatePage(@PathVariable Long id, @Valid @RequestBody PageUpdateDTO dto) {
        Boolean result = pageService.updatePage(id, dto);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "页面更新出错");
        return ResultUtils.success(null);
    }

    /**
     * 删除页面
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deletePage(@PathVariable Long id) {
        Boolean result = pageService.deletePage(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "页面删除出错");
        return ResultUtils.success(null);
    }
}

