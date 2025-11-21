package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.StoreMapper;
import com.qzh.backend.model.entity.Store;
import com.qzh.backend.service.StoreService;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {
}
