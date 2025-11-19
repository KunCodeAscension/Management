package com.qzh.backend.config;

import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("系统报错------BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public BaseResponse<?> bindException(BindException e){
        log.error("BindException",e);
        BindingResult bindingResult = e.getBindingResult();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        List<String> ErrorsList = allErrors.stream().map(ObjectError::getDefaultMessage).toList();
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, Arrays.toString(ErrorsList.toArray()));
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> businessExceptionHandler(Exception e) {
        log.error("系统报错------Exception", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}