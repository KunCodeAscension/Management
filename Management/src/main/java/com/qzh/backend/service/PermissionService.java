package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.permission.PermissionCreateDTO;
import com.qzh.backend.model.dto.permission.PermissionQueryDto;
import com.qzh.backend.model.dto.permission.PermissionUpdateDTO;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.vo.PermissionVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface PermissionService extends IService<Permission> {

    /**
     * 获取权限列表
     */
    Page<PermissionVO> getPermissionList(PermissionQueryDto dto);

    /**
     * 创建权限
     */
    Long createPermission(PermissionCreateDTO createDTO,HttpServletRequest request);

    /**
     * 更新权限信息
     */
    Boolean updatePermission(Long id, PermissionUpdateDTO updateDTO);

    /**
     * 删除权限
     */
    Boolean deletePermission(Long id);

    /**
     * 根据权限ID获取权限详情VO
     */
    PermissionVO getPermissionDetailById(Long id);

    List<Permission> getUserPermissions(HttpServletRequest request);
}
