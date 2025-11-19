package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.RoleMapper;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleSerciceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}
