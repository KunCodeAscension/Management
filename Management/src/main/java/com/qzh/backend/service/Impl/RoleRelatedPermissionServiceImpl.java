package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.RoleRelatedPermissionMapper;
import com.qzh.backend.model.entity.RoleRelatedPermission;
import com.qzh.backend.service.RoleRelatedPermissionService;
import org.springframework.stereotype.Service;

@Service
public class RoleRelatedPermissionServiceImpl extends ServiceImpl<RoleRelatedPermissionMapper, RoleRelatedPermission> implements RoleRelatedPermissionService {
}
