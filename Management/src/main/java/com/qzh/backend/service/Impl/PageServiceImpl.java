package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.PageMapper;
import com.qzh.backend.model.dto.page.PageCreateDTO;
import com.qzh.backend.model.dto.page.PageEditPermissionDTO;
import com.qzh.backend.model.dto.page.PageQueryDTO;
import com.qzh.backend.model.dto.page.PageUpdateDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.vo.PageVO;
import com.qzh.backend.model.vo.PermissionVO;
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

@Service
@RequiredArgsConstructor
public class PageServiceImpl extends ServiceImpl<PageMapper, PageInfo> implements PageService {

    private final PageRelatedPermissionService pageRelatedPermissionService;

    private final PermissionService permissionService;

    private final GetLoginUserUtil getLoginUserUtil;

    private final UserRelatedRoleService userRelatedRoleService;

    private final RoleRelatedPageService roleRelatedPageService;


    @Override
    public Page<PageVO> getPageList(PageQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<PageInfo> pagePage = this.page(new Page<>(current, size), PageQueryDTO.getQueryWrapper(queryDTO));
        List<PageInfo> pageList = pagePage.getRecords();
        Page<PageVO> pageVOPage = new Page<>(current, size, pagePage.getTotal());
        if (CollectionUtils.isEmpty(pageList)) {
            pageVOPage.setRecords(List.of());
            return pageVOPage;
        }
        List<PageVO> pageVOList = PageVO.toPageVOList(pageList);

        // 批量查询页面下关联的权限信息，并填充到 PageVO 中
        List<Long> pageIds = pageList.stream()
                .map(PageInfo::getId)
                .toList();

        List<PageRelatedPermission> pagePermissionRelations = pageRelatedPermissionService.list(
                new LambdaQueryWrapper<PageRelatedPermission>()
                        .in(PageRelatedPermission::getPageId, pageIds)
        );
        if (!CollectionUtils.isEmpty(pagePermissionRelations)) {
            List<Long> permissionIds = pagePermissionRelations.stream()
                    .map(PageRelatedPermission::getPermissionId)
                    .distinct()
                    .collect(Collectors.toList());

            List<Permission> permissionList = permissionService.list(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getId, permissionIds)
            );
            Map<Long, Permission> permissionIdMap = permissionList.stream()
                    .collect(Collectors.toMap(
                            Permission::getId,
                            permission -> permission,
                            (oldVal, newVal) -> oldVal
                    ));
            Map<Long, List<Permission>> pageIdToPermissionsMap = pagePermissionRelations.stream()
                    .collect(Collectors.groupingBy(
                            PageRelatedPermission::getPageId,
                            Collectors.mapping(
                                    relation -> permissionIdMap.get(relation.getPermissionId()),
                                    Collectors.filtering(Objects::nonNull, Collectors.toList())
                            )
                    ));
            pageVOList.forEach(pageVO -> {
                List<Permission> permissions = pageIdToPermissionsMap.getOrDefault(pageVO.getId(), Collections.emptyList());
                pageVO.setPermissions(PermissionVO.toPermissionVOList(permissions));
            });
        } else {
            pageVOList.forEach(vo -> vo.setPermissions(Collections.emptyList()));
        }
        pageVOPage.setRecords(pageVOList);
        return pageVOPage;
    }

    @Override
    public PageVO getPageDetailById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        PageInfo pageInfo = this.getById(id);
        ThrowUtils.throwIf(pageInfo == null, ErrorCode.NOT_FOUND_ERROR, "页面不存在");
        PageVO pageVO = PageVO.toPageVO(pageInfo);
        // 查询页面-权限关联关系
        List<PageRelatedPermission> pagePermissionRelations = pageRelatedPermissionService.list(
                new LambdaQueryWrapper<PageRelatedPermission>()
                        .eq(PageRelatedPermission::getPageId, id) // 单个pageId精准查询
        );

        // 处理权限数据并注入 PageVO
        if (!CollectionUtils.isEmpty(pagePermissionRelations)) {
            List<Long> permissionIds = pagePermissionRelations.stream()
                    .map(PageRelatedPermission::getPermissionId)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询权限详情
            List<Permission> permissionList = permissionService.list(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getId, permissionIds)
            );

            // 转换为 PermissionVO 并设置到 PageVO
            List<PermissionVO> permissionVOList = PermissionVO.toPermissionVOList(permissionList);
            pageVO.setPermissions(permissionVOList);
        } else {
            // 无关联权限时，设置空列表（避免NPE）
            pageVO.setPermissions(Collections.emptyList());
        }
        return pageVO;
    }

    @Override
    public Long createPage(PageCreateDTO createDTO,HttpServletRequest request) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);

        // 唯一性校验
        boolean b = this.count(new LambdaQueryWrapper<PageInfo>()
                .eq(PageInfo::getName,createDTO.getName())
                .eq(PageInfo::getComponent,createDTO.getComponent())
                .eq(PageInfo::getPath,createDTO.getPath())
                .eq(PageInfo::getParentId,createDTO.getParentId())
        ) > 0;
        ThrowUtils.throwIf(b,ErrorCode.PARAMS_ERROR,"页面已存在");
        User loginUser = getLoginUserUtil.getLoginUser(request);
        PageInfo page = new PageInfo();
        page.setParentId(createDTO.getParentId());
        page.setName(createDTO.getName());
        page.setPath(createDTO.getPath());
        page.setComponent(createDTO.getComponent());
        page.setIcon(createDTO.getIcon());
        page.setOrderNum(createDTO.getOrderNum() != null ? createDTO.getOrderNum() : 0);
        page.setVisible(createDTO.getVisible() != null ? createDTO.getVisible() : 1);
        page.setMeta(createDTO.getMeta());
        page.setCreateBy(loginUser.getId());
        boolean save = this.save(page);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "新增页面出错");
        return page.getId();
    }

    @Override
    public Boolean updatePage(Long id, PageUpdateDTO updateDTO) {
        ThrowUtils.throwIf(id <= 0 || updateDTO == null, ErrorCode.PARAMS_ERROR);
        PageInfo page = this.getById(id);
        ThrowUtils.throwIf(page == null, ErrorCode.NOT_FOUND_ERROR, "页面不存在");

        // 唯一性校验
        boolean b = this.count(new LambdaQueryWrapper<PageInfo>()
                .eq(PageInfo::getName,updateDTO.getName())
                .eq(PageInfo::getComponent,updateDTO.getComponent())
                .eq(PageInfo::getPath,updateDTO.getPath())
                .eq(PageInfo::getParentId,updateDTO.getParentId())
        ) > 0;
        ThrowUtils.throwIf(b,ErrorCode.PARAMS_ERROR,"页面已存在");
        page.setParentId(updateDTO.getParentId());
        page.setName(updateDTO.getName());
        page.setPath(updateDTO.getPath());
        page.setComponent(updateDTO.getComponent());
        page.setIcon(updateDTO.getIcon());
        page.setOrderNum(updateDTO.getOrderNum());
        page.setVisible(updateDTO.getVisible());
        page.setMeta(updateDTO.getMeta());
        return this.updateById(page);
    }

    @Override
    public Boolean deletePage(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        PageInfo page = this.getById(id);
        ThrowUtils.throwIf(page == null, ErrorCode.NOT_FOUND_ERROR, "页面不存在");
        
        // 检查是否有子页面
        long childCount = this.count(new LambdaQueryWrapper<PageInfo>()
                .eq(PageInfo::getParentId, id));
        ThrowUtils.throwIf(childCount > 0, ErrorCode.PARAMS_ERROR, "该页面下存在子页面，无法删除");
        return this.removeById(id);
    }

    @Override
    public List<PageVO> getAllPageWithPermissions() {
        // 查询所有页面（无分页，按orderNum排序，保持菜单层级顺序）
        List<PageInfo> allPageList = this.list(
                new LambdaQueryWrapper<PageInfo>()
                        .orderByAsc(PageInfo::getOrderNum)
        );

        if (CollectionUtils.isEmpty(allPageList)) {
            return Collections.emptyList();
        }

        // 转换为 PageVO 基础信息
        List<PageVO> allPageVOList = PageVO.toPageVOList(allPageList);

        // 批量查询权限关联（复用 getPageList 的高效查询逻辑，避免循环查库）
        List<Long> allPageIds = allPageList.stream()
                .map(PageInfo::getId)
                .collect(Collectors.toList());

        // 查询所有页面-权限关联关系
        List<PageRelatedPermission> pagePermissionRelations = pageRelatedPermissionService.list(
                new LambdaQueryWrapper<PageRelatedPermission>()
                        .in(PageRelatedPermission::getPageId, allPageIds)
        );

        // 处理权限数据并注入 PageVO
        if (!CollectionUtils.isEmpty(pagePermissionRelations)) {
            // 提取去重的权限ID
            List<Long> permissionIds = pagePermissionRelations.stream()
                    .map(PageRelatedPermission::getPermissionId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询权限详情
            List<Permission> permissionList = permissionService.list(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getId, permissionIds)
            );

            // 构建权限ID->权限对象的映射（优化查询效率）
            Map<Long, Permission> permissionIdMap = permissionList.stream()
                    .collect(Collectors.toMap(
                            Permission::getId,
                            permission -> permission,
                            (oldVal, newVal) -> oldVal
                    ));

            // 构建页面ID->权限列表的映射
            Map<Long, List<PermissionVO>> pageIdToPermissionVOMap = pagePermissionRelations.stream()
                    .collect(Collectors.groupingBy(
                            PageRelatedPermission::getPageId,
                            Collectors.mapping(
                                    relation -> PermissionVO.toPermissionVO(permissionIdMap.get(relation.getPermissionId())),
                                    Collectors.filtering(Objects::nonNull, Collectors.toList())
                            )
                    ));

            // 给每个 PageVO 注入对应的权限列表
            allPageVOList.forEach(pageVO -> {
                List<PermissionVO> permissions = pageIdToPermissionVOMap.getOrDefault(pageVO.getId(), Collections.emptyList());
                pageVO.setPermissions(permissions);
            });
        } else {
            // 无任何权限关联时，统一设置空列表（避免NPE）
            allPageVOList.forEach(vo -> vo.setPermissions(Collections.emptyList()));
        }
        return allPageVOList;
    }

    @Override
    public Boolean addPagePermission(Long pageId, Long permissionId,HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(pageId <= 0 || permissionId <= 0, ErrorCode.PARAMS_ERROR, "页面ID或者权限ID参数错误");

        // 校验页面和权限是否存在
        PageInfo page = this.getById(pageId);
        ThrowUtils.throwIf(page == null, ErrorCode.NOT_FOUND_ERROR, "页面不存在");
        Permission permission = permissionService.getById(permissionId);
        ThrowUtils.throwIf(permission == null, ErrorCode.NOT_FOUND_ERROR, "权限不存在");
        // 校验是否已存在关联（避免重复添加）
        boolean exists = pageRelatedPermissionService.count(
                new LambdaQueryWrapper<PageRelatedPermission>()
                        .eq(PageRelatedPermission::getPageId, pageId)
                        .eq(PageRelatedPermission::getPermissionId, permissionId)
        ) > 0;
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "该页面已关联此权限");
        // 新增关联关系
        User loginUser = getLoginUserUtil.getLoginUser(request);
        PageRelatedPermission relation = new PageRelatedPermission();
        relation.setPageId(pageId);
        relation.setPermissionId(permissionId);
        relation.setCreateBy(loginUser.getId());
        return pageRelatedPermissionService.save(relation);
    }

    /**
     * 2. 页面-权限修改接口（覆盖式更新：先删后加）
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保证原子性
    public Boolean updatePagePermissions(Long pageId, PageEditPermissionDTO permissionDTO,HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(pageId <= 0 || permissionDTO == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        List<Long> permissionIds = permissionDTO.getPermissionIds();

        // 校验页面是否存在
        PageInfo page = this.getById(pageId);
        ThrowUtils.throwIf(page == null, ErrorCode.NOT_FOUND_ERROR, "页面不存在");

        // 校验权限ID合法性（可选，提升健壮性）
        if (!CollectionUtils.isEmpty(permissionIds)) {
            long validCount = permissionService.count(
                    new LambdaQueryWrapper<Permission>().in(Permission::getId, permissionIds)
            );
            ThrowUtils.throwIf(validCount != permissionIds.size(), ErrorCode.PARAMS_ERROR, "存在无效的权限ID");
        }

        // 先删除：该页面的所有旧权限关联
        pageRelatedPermissionService.remove(
                new LambdaQueryWrapper<PageRelatedPermission>()
                        .eq(PageRelatedPermission::getPageId, pageId)
        );

        // 批量添加新权限关联
        User loginUser = getLoginUserUtil.getLoginUser(request);
        boolean saveSuccess = true;
        if (!CollectionUtils.isEmpty(permissionIds)) {
            List<PageRelatedPermission> relationList = permissionIds.stream()
                    .map(permId -> {
                        PageRelatedPermission relation = new PageRelatedPermission();
                        relation.setPageId(pageId);
                        relation.setPermissionId(permId);
                        relation.setCreateBy(loginUser.getId());
                        return relation;
                    })
                    .collect(Collectors.toList());

            saveSuccess = pageRelatedPermissionService.saveBatch(relationList);
        }

        return saveSuccess;
    }

    @Override
    public List<PageInfo> getUserPage(HttpServletRequest request) {
        User loginUser = getLoginUserUtil.getLoginUser(request);
        Long userId = loginUser.getId();
        List<UserRelatedRole> userRoleRelations = userRelatedRoleService.list(
                new LambdaQueryWrapper<UserRelatedRole>()
                        .eq(UserRelatedRole::getUserId, userId)
        );
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            return Collections.emptyList();
        }
        //提取角色ID列表
        List<Long> roleIds = userRoleRelations.stream()
                .map(UserRelatedRole::getRoleId)
                .distinct()
                .toList();
        //查询所有角色关联的页面ID
        List<RoleRelatedPage> rolePageRelations = roleRelatedPageService.list(
                new LambdaQueryWrapper<RoleRelatedPage>()
                        .in(RoleRelatedPage::getRoleId, roleIds)
        );
        if (CollectionUtils.isEmpty(rolePageRelations)) {
            return Collections.emptyList();
        }
        // 提取页面ID列表
        List<Long> pageIds = rolePageRelations.stream()
                .map(RoleRelatedPage::getPageId)
                .distinct()
                .collect(Collectors.toList());
        // 查询完整的页面信息
        return this.list(
                new LambdaQueryWrapper<PageInfo>()
                        .in(PageInfo::getId, pageIds)
                        .orderByAsc(PageInfo::getOrderNum)
        );
    }
}

