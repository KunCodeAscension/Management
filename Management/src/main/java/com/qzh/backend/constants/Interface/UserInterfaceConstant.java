package com.qzh.backend.constants.Interface;

public interface UserInterfaceConstant {

    // 用户列表查询（GET /user/list）
    String USER_LIST_GET = "GET:/user/list";

    // 用户详情查询（GET /user/{id}）
    String USER_DETAIL_GET = "GET:/user/:id";

    // 新增用户（POST /user）
    String USER_CREATE_POST = "POST:/user";

    // 更新用户信息（PUT /user/{id}）
    String USER_UPDATE_PUT = "PUT:/user/:id";

    // 重置用户密码（POST /user/{id}/reset-password）
    String USER_RESET_PASSWORD_POST = "POST:/user/:id/reset-password";

    // 批量更新用户状态（POST /user/batch-status）
    String USER_BATCH_STATUS_POST = "POST:/user/batch-status";

    // 删除用户（DELETE /user/{id}）
    String USER_DELETE_DELETE = "DELETE:/user/:id";

    // 用户登录（POST /user/login）
    String USER_LOGIN_POST = "POST:/user/login";

    // 用户注册（POST /user/register）
    String USER_REGISTER_POST = "POST:/user/register";
}
