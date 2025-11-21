package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.OperationLogMapper;
import com.qzh.backend.model.entity.OperationLog;
import com.qzh.backend.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
