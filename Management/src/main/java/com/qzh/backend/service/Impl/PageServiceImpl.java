package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.PageMapper;
import com.qzh.backend.model.dto.page.PageCreateDTO;
import com.qzh.backend.model.dto.page.PageQueryDTO;
import com.qzh.backend.model.dto.page.PageUpdateDTO;
import com.qzh.backend.model.entity.PageInfo;
import com.qzh.backend.model.entity.PageRelatedPermission;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.vo.PageVO;
import com.qzh.backend.model.vo.PermissionVO;
import com.qzh.backend.service.PageRelatedPermissionService;
import com.qzh.backend.service.PageService;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public Long createPage(PageCreateDTO createDTO) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);

        // 唯一性校验
        boolean b = this.count(new LambdaQueryWrapper<PageInfo>()
                .eq(PageInfo::getName,createDTO.getName())
                .eq(PageInfo::getComponent,createDTO.getComponent())
                .eq(PageInfo::getPath,createDTO.getPath())
                .eq(PageInfo::getParentId,createDTO.getParentId())
        ) > 0;
        ThrowUtils.throwIf(b,ErrorCode.PARAMS_ERROR,"页面已存在");
        PageInfo page = new PageInfo();
        page.setParentId(createDTO.getParentId());
        page.setName(createDTO.getName());
        page.setPath(createDTO.getPath());
        page.setComponent(createDTO.getComponent());
        page.setIcon(createDTO.getIcon());
        page.setOrderNum(createDTO.getOrderNum() != null ? createDTO.getOrderNum() : 0);
        page.setVisible(createDTO.getVisible() != null ? createDTO.getVisible() : 1);
        page.setMeta(createDTO.getMeta());

        // TODO 创建人ID字段填充
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
}

