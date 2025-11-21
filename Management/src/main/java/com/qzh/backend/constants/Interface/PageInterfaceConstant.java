package com.qzh.backend.constants.Interface;

/**
 * 页面模块接口常量类（与 PageController 一一对应）
 * 格式：METHOD:PATH（完整路径 = 基础路径 /page + 方法路径）
 */
public interface PageInterfaceConstant {

    // 页面列表分页查询（GET /page/list）
    String PAGE_LIST_GET = "GET:/page/list";

    // 页面详情查询（GET /page/{id}）
    String PAGE_DETAIL_GET = "GET:/page/:id";

    // 新增页面（POST /page）
    String PAGE_CREATE_POST = "POST:/page";

    // 更新页面（PUT /page/{id}）
    String PAGE_UPDATE_PUT = "PUT:/page/:id";

    // 删除页面（DELETE /page/{id}）
    String PAGE_DELETE_DELETE = "DELETE:/page/:id";

    // 查询所有页面（含权限）（GET /page/all）
    String PAGE_ALL_GET = "GET:/page/all";

    // 给页面分配单个权限（POST /page/{pageId}/permission/{permissionId}）
    String PAGE_ASSIGN_SINGLE_PERMISSION_POST = "POST:/page/:id/permission/:id";

    // 批量更新页面权限（PUT /page/{pageId}/permission）
    String PAGE_ASSIGN_BATCH_PERMISSION_PUT = "PUT:/page/:id/permission";

    // 根据登录用户查询可访问页面（POST /page/user）
    String PAGE_USER_GET = "POST:/page/user";

}