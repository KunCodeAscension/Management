package com.qzh.backend.config;

import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.UserMapper;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.enums.UserStatusEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.qzh.backend.constants.UserConstant.USER_LOGIN_EXPIRE_TIME;
import static com.qzh.backend.constants.UserConstant.USER_LOGIN_STATE;


/**
 * 登录拦截器：拦截未登录用户的请求
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    /**
     * 请求处理前执行：判断用户是否登录
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 Session 中获取登录用户信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        Long expireTime = (Long) request.getSession().getAttribute(USER_LOGIN_EXPIRE_TIME);
        // 未登录：抛出异常或直接返回 40100
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录或不存在");
        }
        Long userId = user.getId();
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录或不存在");
        }
        if (currentUser.getStatus().equals(UserStatusEnum.Disable.getValue())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号被禁用");
        }
        // 校验登录状态是否过期
        if (System.currentTimeMillis() > expireTime) {
            // 过期：清除 Session 属性，要求重新登录
            request.getSession().removeAttribute(USER_LOGIN_STATE);
            request.getSession().removeAttribute(USER_LOGIN_EXPIRE_TIME);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已过期，请重新登录");
        }
        long newExpireTime = System.currentTimeMillis() + 3600 * 1000; // 续期1小时
        request.getSession().setAttribute(USER_LOGIN_EXPIRE_TIME, newExpireTime);
        request.getSession().setAttribute(USER_LOGIN_STATE,currentUser);
        return true;
    }
}