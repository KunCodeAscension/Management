package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.role.*;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.model.vo.RoleVO;
import jakarta.servlet.http.HttpServletRequest;

public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表
     */
    Page<RoleVO> getRolePage(RoleQueryDTO queryDTO);

    /**
     * 根据ID查询角色详情（含关联权限）
     */
    RoleVO getRoleDetailById(Long id);

    /**
     * 创建角色
     */
    Long createRole(RoleCreateDTO createDTO, HttpServletRequest request);

    /**
     * 更新角色信息
     */
    Boolean updateRole(Long id, RoleUpdateDTO updateDTO);

    /**
     * 删除角色（含权限关联解除）
     */
    Boolean deleteRole(Long id);

    /**
     * 给角色分配权限（先删后加，覆盖式更新）
     */
    Boolean assignRolePermissions(Long roleId, RoleAddPermissionDTO permissionDTO,HttpServletRequest request);

    /**
     * 给角色分配页面
     */
    Boolean assignRolePages(Long roleId, RolePageAssignDTO assignDTO,HttpServletRequest request);

}
