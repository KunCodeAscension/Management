package com.qzh.backend.constants.Interface;

/**
 * 角色模块接口常量类（与 RoleController 一一对应）
 * 格式：METHOD:PATH（完整路径 = 基础路径 /role + 方法路径）
 */
public interface RoleInterfaceConstant {

    // 角色列表查询（GET /role/list）
    String ROLE_LIST_GET = "GET:/role/list";

    // 新增角色（POST /role）
    String ROLE_CREATE_POST = "POST:/role";

    // 角色详情查询（GET /role/{id}）
    String ROLE_DETAIL_GET = "GET:/role/:id";

    // 更新角色信息（PUT /role/{id}）
    String ROLE_UPDATE_PUT = "PUT:/role/:id";

    // 删除角色（DELETE /role/{id}）
    String ROLE_DELETE_DELETE = "DELETE:/role/:id";

    // 给角色分配权限（PUT /role/{roleId}/permission）
    String ROLE_ASSIGN_PERMISSION_PUT = "PUT:/role/:id/permission";

    // 给角色分配页面（POST /role/{roleId}/assign-pages）
    String ROLE_ASSIGN_PAGE_POST = "POST:/role/:id/assign-pages";

}