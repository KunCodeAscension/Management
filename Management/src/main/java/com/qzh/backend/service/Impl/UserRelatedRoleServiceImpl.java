package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.UserRelatedRoleMapper;
import com.qzh.backend.model.entity.UserRelatedRole;
import com.qzh.backend.service.UserRelatedRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRelatedRoleServiceImpl extends ServiceImpl<UserRelatedRoleMapper, UserRelatedRole> implements UserRelatedRoleService {
}
