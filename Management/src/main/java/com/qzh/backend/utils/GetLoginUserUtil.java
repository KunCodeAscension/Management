package com.qzh.backend.utils;

import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.UserMapper;
import com.qzh.backend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.qzh.backend.constants.UserConstant.USER_LOGIN_STATE;

@RequiredArgsConstructor
@Component
public class GetLoginUserUtil {

    private final UserMapper userMapper;

    public User getLoginUser(HttpServletRequest request) {
        // 从 Session 获取登录状态
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        // 未登录校验
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录，请先登录");
        }
        return currentUser;
    }

}
