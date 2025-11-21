package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.operationLog.OperationLogQueryDTO;
import com.qzh.backend.model.entity.OperationLog;
import com.qzh.backend.service.OperationLogService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("log")
@RequiredArgsConstructor
public class OperationController {

    private final OperationLogService operationLogService;

    @PostMapping
    public BaseResponse<Page<OperationLog>> getOperationLogPage(@RequestBody OperationLogQueryDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        int current = dto.getCurrent();
        int size = dto.getSize();
        Page<OperationLog> page = new Page<>(current, size);
        Page<OperationLog> operationLogPage = operationLogService.page(page, OperationLogQueryDTO.getQueryWrapper(dto));
        return ResultUtils.success(operationLogPage);
    }

}
