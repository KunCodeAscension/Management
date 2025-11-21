package com.qzh.backend.aop;

import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.model.entity.OperationLog;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.service.OperationLogService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.GetMessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

import static com.qzh.backend.constants.OperationStatusConstant.OPERATION_STATUS_FAILURE;
import static com.qzh.backend.constants.OperationStatusConstant.OPERATION_STATUS_SUCCESS;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {

    private final GetLoginUserUtil getLoginUserUtil;

    private final OperationLogService operationLogService;

    @AfterReturning("@annotation(logInfoRecord)")
    public void recordLogAfterReturning(JoinPoint joinPoint, LogInfoRecord logInfoRecord) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.warn("操作日志记录失败：无法获取请求上下文");
            return;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        // 用户信息
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // IP地址
        String ipAddress = GetMessageUtils.getClientIpAddress(request);
        // 设备 浏览器
        String device = GetMessageUtils.getDevice(request);
        String browser = GetMessageUtils.getBrowser(request);
        String os = GetMessageUtils.getOs(request);
        OperationLog operationLog = new OperationLog();
        operationLog.setOperatorId(loginUser.getId());
        operationLog.setOperatorDevice(String.format("%s/%s/%s", device, os, browser));
        operationLog.setOperatorIp(ipAddress);
        operationLog.setSystemModule(logInfoRecord.SystemModule());
        operationLog.setOperationContent(String.format("方法入参:%s", Arrays.toString(joinPoint.getArgs())));
        operationLog.setOperationResult(OPERATION_STATUS_SUCCESS);
        operationLogService.save(operationLog);
    }

    /**
     * 异常通知：方法执行失败后记录日志（含错误信息）
     */
    @AfterThrowing(value = "@annotation(logInfoRecord)",throwing = "e")
    public void recordLogAfterThrowing(JoinPoint joinPoint, LogInfoRecord logInfoRecord, Exception e) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.warn("操作日志记录失败：无法获取请求上下文");
            return;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        // 用户信息
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // IP地址
        String ipAddress = GetMessageUtils.getClientIpAddress(request);
        // 设备 浏览器
        String device = GetMessageUtils.getDevice(request);
        String browser = GetMessageUtils.getBrowser(request);
        String os = GetMessageUtils.getOs(request);
        OperationLog operationLog = new OperationLog();
        operationLog.setOperatorId(loginUser.getId());
        operationLog.setOperatorDevice(String.format("%s/%s/%s", device, os, browser));
        operationLog.setOperatorIp(ipAddress);
        operationLog.setSystemModule(logInfoRecord.SystemModule());
        operationLog.setOperationContent(String.format("方法入参:%s", Arrays.toString(joinPoint.getArgs())));
        operationLog.setOperationResult(OPERATION_STATUS_FAILURE);
        operationLog.setErrorMsg(e.getMessage());
        operationLogService.save(operationLog);
    }

}
