package com.qzh.backend.constants.Interface;

/**
 * 权限模块接口常量类（与 PermissionController 一一对应）
 * 格式：METHOD:PATH（完整路径 = 基础路径 /permission + 方法路径）
 */
public interface PermissionInterfaceConstant {

    // 权限列表查询（GET /permission/list）
    String PERMISSION_LIST_GET = "GET:/permission/list";

    // 新增权限（POST /permission）
    String PERMISSION_CREATE_POST = "POST:/permission";

    // 更新权限（PUT /permission/{id}）
    String PERMISSION_UPDATE_PUT = "PUT:/permission/:id";

    // 删除权限（DELETE /permission/{id}）
    String PERMISSION_DELETE_DELETE = "DELETE:/permission/:id";

    // 权限详情查询（GET /permission/{id}）
    String PERMISSION_DETAIL_GET = "GET:/permission/:id";

    // 根据登录用户查询权限（POST /permission/user）
    String PERMISSION_USER_GET = "POST:/permission/user";

}