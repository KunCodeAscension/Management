package com.qzh.backend.aop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.entity.RoleRelatedPermission;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.entity.UserRelatedRole;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.service.RoleRelatedPermissionService;
import com.qzh.backend.service.UserRelatedRoleService;
import com.qzh.backend.utils.GetLoginUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthCheckAspect {

    private final GetLoginUserUtil getLoginUserUtil;

    private final UserRelatedRoleService userRelatedRoleService;

    private final RoleRelatedPermissionService roleRelatedPermissionService;

    private final PermissionService permissionService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取当前请求的 HttpServletRequest（用于获取登录用户）
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.error("权限校验失败：无法获取请求上下文");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "权限校验失败");
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        User loginUser = getLoginUserUtil.getLoginUser(request);
        Long userId = loginUser.getId();
        String requiredInterfaceName = authCheck.interfaceName();
        if (requiredInterfaceName.isBlank()) {
            log.error("权限校验失败：@AuthCheck 注解的 interfaceName 不能为空");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "权限校验失败：接口名未指定");
        }
        // 查询用户拥有的所有权限名称集合（用户→角色→权限）
        Set<String> userPermissionNames = getUserPermissionNames(userId);
        log.info("用户ID={} 拥有的权限集合：{}", userId, userPermissionNames);
        // 校验用户是否拥有目标接口权限
        if (!userPermissionNames.contains(requiredInterfaceName)) {
            log.warn("权限校验失败：用户ID={} 缺少接口权限={}", userId, requiredInterfaceName);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该接口");
        }
        return joinPoint.proceed();
    }

    private Set<String> getUserPermissionNames(Long userId) {
        // 查询用户关联的所有角色ID
        List<UserRelatedRole> userRoleList = userRelatedRoleService.list(
                new LambdaQueryWrapper<UserRelatedRole>()
                        .eq(UserRelatedRole::getUserId, userId)
        );
        if (userRoleList.isEmpty()) {
            return Collections.emptySet(); // 无角色 → 无权限
        }
        List<Long> roleIdList = userRoleList.stream()
                .map(UserRelatedRole::getRoleId)
                .collect(Collectors.toList());
        // 查询所有角色关联的权限ID
        List<RoleRelatedPermission> rolePermissionList = roleRelatedPermissionService.list(
                new LambdaQueryWrapper<RoleRelatedPermission>()
                        .in(RoleRelatedPermission::getRoleId, roleIdList)
        );
        if (rolePermissionList.isEmpty()) {
            return Collections.emptySet(); // 角色无关联权限 → 无权限
        }
        List<Long> permissionIdList = rolePermissionList.stream()
                .map(RoleRelatedPermission::getPermissionId)
                .collect(Collectors.toList());

        List<Permission> permissionList = permissionService.list(
                new LambdaQueryWrapper<Permission>()
                        .in(Permission::getId, permissionIdList)
        );
        return permissionList.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

}
