package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.RoleMapper;
import com.qzh.backend.model.dto.role.RoleCreateDTO;
import com.qzh.backend.model.dto.role.RoleQueryDTO;
import com.qzh.backend.model.dto.role.RoleUpdateDTO;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.model.entity.RoleRelatedPermission;
import com.qzh.backend.model.vo.RoleVO;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.service.RoleRelatedPermissionService;
import com.qzh.backend.service.RoleService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoleSerciceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleRelatedPermissionService roleRelatedPermissionService;

    private final PermissionService permissionService;

    @Override
    public Page<RoleVO> getRolePage(RoleQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO==null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Role> rolePage = this.page(new Page<>(current, size), RoleQueryDTO.getQueryWrapper(queryDTO));
        List<Role> roleList = rolePage.getRecords();
        Page<RoleVO> roleVOPage = new Page<>(current, size, rolePage.getTotal());
        // 查询数据为空直接返回
        if (CollectionUtils.isEmpty(roleList)) {
            roleVOPage.setRecords(List.of());
            return roleVOPage;
        }
        // 转为 VOList
        List<RoleVO> roleVOList = RoleVO.toRoleVOList(roleList);
        // 批量查询角色-权限关联关系（sys_role_permission）
        List<Long> roleIds = roleList.stream()
                .map(Role::getId)
                .toList();
        // 查询出所有角色 与 权限的关联
        List<RoleRelatedPermission> rolePermissionList = roleRelatedPermissionService.list(
                new LambdaQueryWrapper<RoleRelatedPermission>()
                        .in(RoleRelatedPermission::getRoleId, roleIds)
        );
        // 不为空
        if (!CollectionUtils.isEmpty(rolePermissionList)) {
            // 提取所有权限ID，批量查询权限详情（sys_permission）
            List<Long> permissionIds = rolePermissionList.stream()
                    .map(RoleRelatedPermission::getPermissionId)
                    .distinct() // 去重，减少查询压力
                    .collect(Collectors.toList());

            // 假设 PermissionService 已存在，用于查询权限详情
            List<Permission> permissionList = permissionService.list(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getId, permissionIds)
            );
            // 构建映射：权限ID -> 权限实体
            Map<Long, Permission> permissionIdMap = permissionList.stream()
                    .collect(Collectors.toMap(
                            Permission::getId,
                            permission -> permission,
                            (oldVal, newVal) -> oldVal // 避免权限ID重复（理论上不会）
                    ));
            // 构建映射：角色ID -> 对应的权限列表
            Map<Long, List<Permission>> roleIdToPermissionsMap = rolePermissionList.stream()
                    .collect(Collectors.groupingBy(
                            RoleRelatedPermission::getRoleId, // 按角色ID分组
                            Collectors.mapping(
                                    relation -> permissionIdMap.get(relation.getPermissionId()), // 转换为权限实体
                                    Collectors.filtering(Objects::nonNull, Collectors.toList()) // 过滤无效权限
                            )
                    ));
            // 给每个 RoleVO 填充 permissions 字段
            roleVOList.forEach(roleVO -> {
                List<Permission> permissions = roleIdToPermissionsMap.getOrDefault(roleVO.getId(), Collections.emptyList());
                roleVO.setPermissions(permissions);
            });
        } else {
            roleVOList.forEach(vo -> vo.setPermissions(Collections.emptyList()));
        }
        roleVOPage.setRecords(roleVOList);
        return roleVOPage;

    }

    @Override
    public RoleVO getRoleDetailById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Role role = this.getById(id);
        ThrowUtils.throwIf(role == null,ErrorCode.NOT_FOUND_ERROR,"角色不存在");
        RoleVO roleVO = RoleVO.toRoleVO(role);
        List<RoleRelatedPermission> roleRelations = roleRelatedPermissionService.list(new LambdaQueryWrapper<RoleRelatedPermission>()
                .eq(RoleRelatedPermission::getRoleId, roleVO.getId())
        );
        // 填充权限列表
        if (!CollectionUtils.isEmpty(roleRelations)) {
            // 提取所有权限ID
            List<Long> permissionIds = roleRelations.stream()
                    .map(RoleRelatedPermission::getPermissionId)
                    .distinct()
                    .collect(Collectors.toList());
            // 批量查询权限详情
            List<Permission> permissionList = permissionService.list(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getId, permissionIds)
            );
            // 构建权限ID -> 权限实体的映射
            Map<Long, Permission> permissionMap = permissionList.stream()
                    .collect(Collectors.toMap(
                            Permission::getId,
                            permission -> permission,
                            (oldVal, newVal) -> oldVal // 避免权限ID重复（理论上不会）
                    ));
            // 匹配角色对应的权限列表
            List<Permission> permissions = roleRelations.stream()
                    .map(relation -> permissionMap.get(relation.getPermissionId()))
                    .filter(Objects::nonNull) // 过滤已删除但关联未清理的无效权限
                    .collect(Collectors.toList());
            //给 RoleVO 设置权限列表
            roleVO.setPermissions(permissions);
        } else {
            //无权限关联时，设置空列表（避免前端null异常）
            roleVO.setPermissions(Collections.emptyList());
        }
        return roleVO;
    }

    @Override
    public Long createRole(RoleCreateDTO createDTO) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);
        boolean b = this.count(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, createDTO.getRoleName())) > 0;
        ThrowUtils.throwIf(b,ErrorCode.PARAMS_ERROR,"该角色已存在");
        Role role = new Role();
        role.setRoleName(createDTO.getRoleName());
        role.setDescription(createDTO.getDescription());
        // TODO 创建人ID字段填充
        boolean save = this.save(role);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"新增角色出错");
        return role.getId();
    }

    @Override
    public Boolean updateRole(Long id, RoleUpdateDTO updateDTO) {
        ThrowUtils.throwIf(id <= 0 || updateDTO == null, ErrorCode.PARAMS_ERROR);
        Role role = this.getById(id);
        ThrowUtils.throwIf(role == null,ErrorCode.PARAMS_ERROR,"角色不存在");
        role.setRoleName(updateDTO.getRoleName());
        role.setDescription(updateDTO.getDescription());
        return this.updateById(role);
    }

    @Override
    public Boolean deleteRole(Long id) {
        return this.removeById(id);
    }
}
