package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.PageRelatedPermissionMapper;
import com.qzh.backend.model.entity.PageRelatedPermission;
import com.qzh.backend.service.PageRelatedPermissionService;
import org.springframework.stereotype.Service;

@Service
public class PageRelatedPermissionServiceImpl extends ServiceImpl<PageRelatedPermissionMapper, PageRelatedPermission> implements PageRelatedPermissionService {
}

