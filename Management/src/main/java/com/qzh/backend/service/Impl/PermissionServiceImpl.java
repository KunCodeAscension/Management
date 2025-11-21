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
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.vo.PermissionVO;
import com.qzh.backend.service.*;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PageRelatedPermissionService pageRelatedPermissionService;

    private final GetLoginUserUtil getLoginUserUtil;

    private final UserRelatedRoleService userRelatedRoleService;

    private final RoleRelatedPermissionService roleRelatedPermissionService;

    @Lazy
    @Resource
    private PageService pageService;

    @Override
    public Page<PermissionVO> getPermissionList(PermissionQueryDto dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        int current = dto.getCurrent();
        int size = dto.getSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询权限列表并转换为 PermissionVO
        Page<Permission> permissionPage = this.page(new Page<>(current, size), PermissionQueryDto.getQueryWrapper(dto));
        List<Permission> permissionList = permissionPage.getRecords();
        Page<PermissionVO> permissionVOPage = new Page<>(current, size, permissionPage.getTotal());

        if (CollectionUtils.isEmpty(permissionList)) {
            permissionVOPage.setRecords(List.of());
            return permissionVOPage;
        }
        List<PermissionVO> permissionVOList = PermissionVO.toPermissionVOList(permissionList);
        // 批量查询权限-页面关联关系，填充 pages 字段
        // 提取当前页所有权限ID
        List<Long> permissionIds = permissionList.stream()
                .map(Permission::getId)
                .collect(Collectors.toList());

        //批量查询权限-页面关联表（sys_page_related_permission）
        List<PageRelatedPermission> permissionPageRelations = pageRelatedPermissionService.list(
                new LambdaQueryWrapper<PageRelatedPermission>()
                        .in(PageRelatedPermission::getPermissionId, permissionIds)
        );

        if (!CollectionUtils.isEmpty(permissionPageRelations)) {
            //提取所有页面ID（去重），批量查询页面详情
            List<Long> pageIds = permissionPageRelations.stream()
                    .map(PageRelatedPermission::getPageId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询页面完整实体（PageInfo）
            List<PageInfo> pageList = pageService.list(
                    new LambdaQueryWrapper<PageInfo>()
                            .in(PageInfo::getId, pageIds)
            );

            //构建 页面ID -> 页面实体 的映射（优化匹配效率）
            Map<Long, PageInfo> pageIdToPageMap = pageList.stream()
                    .collect(Collectors.toMap(
                            PageInfo::getId,
                            page -> page,
                            (oldVal, newVal) -> oldVal // 避免页面ID重复
                    ));

            // 构建 权限ID -> 页面实体列表 的映射
            Map<Long, List<PageInfo>> permissionIdToPagesMap = permissionPageRelations.stream()
                    .collect(Collectors.groupingBy(
                            PageRelatedPermission::getPermissionId, // 按权限ID分组
                            Collectors.mapping(
                                    relation -> pageIdToPageMap.get(relation.getPageId()), // 转换为页面实体
                                    Collectors.filtering(Objects::nonNull, Collectors.toList()) // 过滤无效页面
                            )
                    ));
            permissionVOList.forEach(permissionVO -> {
                List<PageInfo> pages = permissionIdToPagesMap.getOrDefault(permissionVO.getId(), Collections.emptyList());
                permissionVO.setPages(pages);
            });
        } else {
            // 无页面关联时，设置空列表（避免 NPE）
            permissionVOList.forEach(vo -> vo.setPages(Collections.emptyList()));
        }
        permissionVOPage.setRecords(permissionVOList);
        return permissionVOPage;
    }

    @Override
    public Long createPermission(PermissionCreateDTO createDTO,HttpServletRequest request) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);
        // 检查权限名称是否已存在
        boolean exists = this.count(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getName, createDTO.getName())) > 0;
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "该权限名称已存在");
        User loginUser = getLoginUserUtil.getLoginUser(request);
        Permission permission = new Permission();
        permission.setName(createDTO.getName());
        permission.setDescription(createDTO.getDescription());
        permission.setCreateBy(loginUser.getId());
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

    @Override
    public PermissionVO getPermissionDetailById(Long id) {
        // 参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "权限ID不能为空");
        // 查询权限基础信息
        Permission permission = this.getById(id);
        ThrowUtils.throwIf(permission == null, ErrorCode.NOT_FOUND_ERROR, "权限不存在");
        // 转换为 PermissionVO（基础信息）
        PermissionVO permissionVO = PermissionVO.toPermissionVO(permission);
        // 查询该权限关联的页面列表（复用 getPermissionList 的关联逻辑）
        // 查询权限-页面关联关系
        List<PageRelatedPermission> permissionPageRelations = pageRelatedPermissionService.list(
                new LambdaQueryWrapper<PageRelatedPermission>()
                        .eq(PageRelatedPermission::getPermissionId, id) // 单个权限ID精准查询
        );
        // 处理页面数据并注入 PermissionVO
        if (!CollectionUtils.isEmpty(permissionPageRelations)) {
            // 提取页面ID
            List<Long> pageIds = permissionPageRelations.stream()
                    .map(PageRelatedPermission::getPageId)
                    .distinct()
                    .collect(Collectors.toList());
            // 批量查询页面详情
            List<PageInfo> pageList = pageService.list(
                    new LambdaQueryWrapper<PageInfo>()
                            .in(PageInfo::getId, pageIds)
            );
            // 过滤无效页面（避免关联已删除的页面）
            List<PageInfo> validPageList = pageList.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            // 给 PermissionVO 填充 pages 字段
            permissionVO.setPages(validPageList);
        } else {
            // 无关联页面时，设置空列表（避免 NPE）
            permissionVO.setPages(Collections.emptyList());
        }
        return permissionVO;
    }

    @Override
    public List<Permission> getUserPermissions(HttpServletRequest request) {
        // 获取登录用户
        User loginUser = getLoginUserUtil.getLoginUser(request);
        Long userId = loginUser.getId();
        // 查询用户关联的所有角色ID
        List<UserRelatedRole> userRoleRelations = userRelatedRoleService.list(
                new LambdaQueryWrapper<UserRelatedRole>()
                        .eq(UserRelatedRole::getUserId, userId)
        );
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            return Collections.emptyList();
        }
        // 提取角色ID列表
        List<Long> roleIds = userRoleRelations.stream()
                .map(UserRelatedRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
        // 查询所有角色关联的权限ID
        List<RoleRelatedPermission> rolePermissionRelations = roleRelatedPermissionService.list(
                new LambdaQueryWrapper<RoleRelatedPermission>()
                        .in(RoleRelatedPermission::getRoleId, roleIds)
        );
        if (CollectionUtils.isEmpty(rolePermissionRelations)) {
            return Collections.emptyList(); // 角色无关联权限
        }
        // 提取权限ID列表
        List<Long> permissionIds = rolePermissionRelations.stream()
                .map(RoleRelatedPermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());
        // 查询完整的权限信息
        return this.list(
                new LambdaQueryWrapper<Permission>()
                        .in(Permission::getId, permissionIds)
        );
    }
}
