package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.PermissionMapper;
import com.qzh.backend.model.dto.permission.PermissionCreateDTO;
import com.qzh.backend.model.dto.permission.PermissionQueryDto;
import com.qzh.backend.model.dto.permission.PermissionUpdateDTO;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.vo.PermissionVO;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.utils.ThrowUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public Page<PermissionVO> getPermissionList(PermissionQueryDto dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        int current = dto.getCurrent();
        int size = dto.getSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Permission> permissionPage = this.page(new Page<>(current, size), PermissionQueryDto.getQueryWrapper(dto));
        List<Permission> permissionList = permissionPage.getRecords();
        Page<PermissionVO> permissionVOPage = new Page<>(current, size, permissionPage.getTotal());
        // 查询数据为空直接返回
        if (CollectionUtils.isEmpty(permissionList)) {
            permissionVOPage.setRecords(List.of());
            return permissionVOPage;
        }
        // 转为 VOList
        List<PermissionVO> permissionVOList = PermissionVO.toPermissionVOList(permissionList);
        permissionVOPage.setRecords(permissionVOList);
        return permissionVOPage;
    }

    @Override
    public Long createPermission(PermissionCreateDTO createDTO) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);
        // 检查权限名称是否已存在
        boolean exists = this.count(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getName, createDTO.getName())) > 0;
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "该权限名称已存在");
        
        Permission permission = new Permission();
        permission.setName(createDTO.getName());
        permission.setDescription(createDTO.getDescription());
        // TODO 创建人ID字段填充
        boolean save = this.save(permission);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "新增权限出错");
        return permission.getId();
    }

    @Override
    public Boolean updatePermission(Long id, PermissionUpdateDTO updateDTO) {
        ThrowUtils.throwIf(id <= 0 || updateDTO == null, ErrorCode.PARAMS_ERROR);
        Permission permission = this.getById(id);
        ThrowUtils.throwIf(permission == null, ErrorCode.NOT_FOUND_ERROR, "权限不存在");
        
        // 如果修改了权限名称，检查新名称是否已存在
        if (!permission.getName().equals(updateDTO.getName())) {
            boolean exists = this.count(new LambdaQueryWrapper<Permission>()
                    .eq(Permission::getName, updateDTO.getName())
                    .ne(Permission::getId, id)) > 0;
            ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "该权限名称已存在");
        }
        
        permission.setName(updateDTO.getName());
        permission.setDescription(updateDTO.getDescription());
        return this.updateById(permission);
    }

    @Override
    public Boolean deletePermission(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Permission permission = this.getById(id);
        ThrowUtils.throwIf(permission == null, ErrorCode.NOT_FOUND_ERROR, "权限不存在");
        return this.removeById(id);
    }
}
