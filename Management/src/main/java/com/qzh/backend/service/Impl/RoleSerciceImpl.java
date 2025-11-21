package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.RoleMapper;
import com.qzh.backend.model.dto.role.*;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.vo.RoleVO;
import com.qzh.backend.service.*;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qzh.backend.constants.RoleNameConstant.*;

@RequiredArgsConstructor
@Service
public class RoleSerciceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleRelatedPermissionService roleRelatedPermissionService;

    private final PermissionService permissionService;

    private final PageService pageService;

    private final RoleRelatedPageService roleRelatedPageService;

    private final GetLoginUserUtil getLoginUserUtil;

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
    public Long createRole(RoleCreateDTO createDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);
        boolean b = this.count(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, createDTO.getRoleName())) > 0;
        ThrowUtils.throwIf(b,ErrorCode.PARAMS_ERROR,"该角色已存在");
        User loginUser = getLoginUserUtil.getLoginUser(request);
        Role role = new Role();
        role.setRoleName(createDTO.getRoleName());
        role.setDescription(createDTO.getDescription());
        role.setCreateBy(loginUser.getId());
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
        Role role = this.getById(id);
        if(ADMIN.equals(role.getRoleName()) || CUSTOMER.equals(role.getRoleName()) || SUPPLIER.equals(role.getRoleName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该角色不可删除");
        }
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保证：删和加原子性
    public Boolean assignRolePermissions(Long roleId, RoleAddPermissionDTO permissionDTO,HttpServletRequest request) {
        // 基础参数校验
        ThrowUtils.throwIf(roleId <= 0, ErrorCode.PARAMS_ERROR, "角色ID不能为空");
        ThrowUtils.throwIf(permissionDTO == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        List<Long> permissionIds = permissionDTO.getPermissionIds();
        // 校验角色是否存在
        Role role = this.getById(roleId);
        ThrowUtils.throwIf(role == null, ErrorCode.NOT_FOUND_ERROR, "角色不存在");

        // 先删除：该角色的所有旧权限关联（核心：覆盖式更新）
        roleRelatedPermissionService.remove(
                new LambdaQueryWrapper<RoleRelatedPermission>()
                        .eq(RoleRelatedPermission::getRoleId, roleId)
        );
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 后添加：批量添加新的权限关联（空列表则只删不加，清空角色权限）
        boolean saveNewPermissions = true;
        if (!CollectionUtils.isEmpty(permissionIds)) {
            // 批量构建角色-权限关联实体
            List<RoleRelatedPermission> rolePermissionRelations = permissionIds.stream()
                    .map(permissionId -> {
                        RoleRelatedPermission relation = new RoleRelatedPermission();
                        relation.setRoleId(roleId);
                        relation.setPermissionId(permissionId);
                        relation.setCreateBy(loginUser.getId());
                        return relation;
                    })
                    .collect(Collectors.toList());

            // 批量保存关联关系
            saveNewPermissions = roleRelatedPermissionService.saveBatch(rolePermissionRelations);
        }
        return saveNewPermissions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保证：删和加原子性
    public Boolean assignRolePages(Long roleId, RolePageAssignDTO assignDTO,HttpServletRequest request) {
        // 基础参数校验
        ThrowUtils.throwIf(roleId <= 0, ErrorCode.PARAMS_ERROR, "角色ID不能为空");
        ThrowUtils.throwIf(assignDTO == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        List<Long> pageIds = assignDTO.getPageIds();
        // 校验角色是否存在
        Role role = this.getById(roleId);
        ThrowUtils.throwIf(role == null, ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        // 校验页面ID是否合法（可选：避免分配不存在的页面）
        if (!CollectionUtils.isEmpty(pageIds)) {
            long validPageCount = pageService.count(
                    new LambdaQueryWrapper<PageInfo>()
                            .in(PageInfo::getId, pageIds)
            );
            ThrowUtils.throwIf(validPageCount != pageIds.size(), ErrorCode.PARAMS_ERROR, "存在无效的页面ID");
        }
        // 先删除：该角色的所有旧页面关联（核心：覆盖式更新）
        roleRelatedPageService.remove(
                new LambdaQueryWrapper<RoleRelatedPage>()
                        .eq(RoleRelatedPage::getRoleId, roleId)
        );
        // 后添加：批量添加新的角色-页面关联
        User loginUser = getLoginUserUtil.getLoginUser(request);
        boolean saveNewRelations = true;
        if (!CollectionUtils.isEmpty(pageIds)) {
            // 批量构建角色-页面关联实体
            List<RoleRelatedPage> relationList = pageIds.stream()
                    .map(pageId -> {
                        RoleRelatedPage relation = new RoleRelatedPage();
                        relation.setRoleId(roleId);
                        relation.setPageId(pageId);
                        relation.setCreateBy(loginUser.getId());
                        return relation;
                    })
                    .collect(Collectors.toList());
            // 批量保存关联关系
            saveNewRelations = roleRelatedPageService.saveBatch(relationList);
        }
        return saveNewRelations;
    }
}
