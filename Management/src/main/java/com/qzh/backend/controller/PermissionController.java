package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.permission.PermissionCreateDTO;
import com.qzh.backend.model.dto.permission.PermissionQueryDto;
import com.qzh.backend.model.dto.permission.PermissionUpdateDTO;
import com.qzh.backend.model.vo.PermissionVO;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/system/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限列表
     * 接口地址: /api/system/permission/list
     * 请求方法: GET
     * 权限要求: 拥有 "权限查询" 权限
     */
    @GetMapping("/list")
    public BaseResponse<Page<PermissionVO>> getPermissionList(PermissionQueryDto dto) {
        Page<PermissionVO> permissionPage = permissionService.getPermissionList(dto);
        return ResultUtils.success(permissionPage);
    }

    /**
     * 添加权限
     * 接口地址: /api/system/permission
     * 请求方法: POST
     * 权限要求: 拥有 "权限分配" 权限
     */
    @PostMapping
    public BaseResponse<Long> createPermission(@Valid @RequestBody PermissionCreateDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Long permissionId = permissionService.createPermission(dto);
        return ResultUtils.success(permissionId);
    }

    /**
     * 编辑权限
     * 接口地址: /api/system/permission/{id}
     * 请求方法: PUT
     * 权限要求: 拥有 "权限编辑" 权限
     */
    @PutMapping("/{id}")
    public BaseResponse<Void> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionUpdateDTO dto) {
        Boolean result = permissionService.updatePermission(id, dto);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "权限更新出错");
        return ResultUtils.success(null);
    }

    /**
     * 删除权限
     * 接口地址: /api/system/permission/{id}
     * 请求方法: DELETE
     * 权限要求: 拥有 "权限删除" 权限
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deletePermission(@PathVariable Long id) {
        Boolean result = permissionService.deletePermission(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "权限删除出错");
        return ResultUtils.success(null);
    }
}

