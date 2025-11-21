package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.permission.PermissionCreateDTO;
import com.qzh.backend.model.dto.permission.PermissionQueryDto;
import com.qzh.backend.model.dto.permission.PermissionUpdateDTO;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.vo.PermissionVO;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.qzh.backend.constants.Interface.PermissionInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.PERMISSION_MODULE;

@RestController
@RequestMapping("permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<PermissionVO>> getPermissionList(PermissionQueryDto dto) {
        Page<PermissionVO> permissionPage = permissionService.getPermissionList(dto);
        return ResultUtils.success(permissionPage);
    }

    /**
     * 添加权限
     */
    @PostMapping
    @LogInfoRecord(SystemModule = PERMISSION_MODULE + ":" + PERMISSION_CREATE_POST)
    public BaseResponse<Long> createPermission(@Valid @RequestBody PermissionCreateDTO dto,HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Long permissionId = permissionService.createPermission(dto,request);
        return ResultUtils.success(permissionId);
    }

    /**
     * 编辑权限
     */
    @LogInfoRecord(SystemModule = PERMISSION_MODULE + ":" + PERMISSION_UPDATE_PUT)
    @PutMapping("/{id}")
    public BaseResponse<Void> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionUpdateDTO dto) {
        Boolean result = permissionService.updatePermission(id, dto);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "权限更新出错");
        return ResultUtils.success(null);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @LogInfoRecord(SystemModule = PERMISSION_MODULE + ":" + PERMISSION_DELETE_DELETE)
    public BaseResponse<Void> deletePermission(@PathVariable Long id) {
        Boolean result = permissionService.deletePermission(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "权限删除出错");
        return ResultUtils.success(null);
    }

    @GetMapping("/{id}")
    public BaseResponse<PermissionVO> getPermissionDetail(@PathVariable Long id) {
        PermissionVO permissionVO = permissionService.getPermissionDetailById(id);
        return ResultUtils.success(permissionVO);
    }

    @PostMapping("user")
    public BaseResponse<List<Permission>> getUserPermissions(HttpServletRequest request) {
        List<Permission> userPermissions = permissionService.getUserPermissions(request);
        return ResultUtils.success(userPermissions);
    }
}

