package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.TransferLogMapper;
import com.qzh.backend.model.entity.TransferLog;
import com.qzh.backend.service.TransferLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferLogServiceImpl extends ServiceImpl<TransferLogMapper, TransferLog> implements TransferLogService {

}