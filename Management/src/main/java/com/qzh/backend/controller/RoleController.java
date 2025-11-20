package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.role.RoleCreateDTO;
import com.qzh.backend.model.dto.role.RoleQueryDTO;
import com.qzh.backend.model.dto.role.RoleUpdateDTO;
import com.qzh.backend.model.vo.RoleVO;
import com.qzh.backend.service.RoleService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("list")
    public BaseResponse<Page<RoleVO>> getRoleList(RoleQueryDTO dto) {
        Page<RoleVO> rolePage = roleService.getRolePage(dto);
        return ResultUtils.success(rolePage);
    }

    @PostMapping
    public BaseResponse<Long> createRole(@Valid RoleCreateDTO dto) {
        Long roleId = roleService.createRole(dto);
        return ResultUtils.success(roleId);
    }

    @GetMapping("{id}")
    public BaseResponse<RoleVO> getRoleById(@PathVariable Long id) {
        RoleVO roleVO = roleService.getRoleDetailById(id);
        return ResultUtils.success(roleVO);
    }

    @PutMapping("{id}")
    public BaseResponse<Void> updateRole(@PathVariable Long id, @RequestBody @Valid RoleUpdateDTO dto) {
        Boolean b = roleService.updateRole(id, dto);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR,"更新角色出错");
        return ResultUtils.success(null);
    }

    @DeleteMapping("{id}")
    public BaseResponse<Void> deleteRole(@PathVariable Long id) {
        Boolean b = roleService.deleteRole(id);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR,"删除角色出错");
        return ResultUtils.success(null);
    }

}
