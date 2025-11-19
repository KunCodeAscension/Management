package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.user.UserCreateDTO;
import com.qzh.backend.model.dto.user.UserQueryDTO;
import com.qzh.backend.model.dto.user.UserUpdateDTO;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {
    /**
     * 分页查询用户列表
     * @param queryDTO 查询参数
     * @return 分页用户列表（含关联信息）
     */
    Page<UserVO> getUserPage(UserQueryDTO queryDTO);

    /**
     * 根据ID查询用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    UserVO getUserDetailById(Long id);

    /**
     * 创建用户（含角色关联）
     * @param createDTO 创建参数
     * @return 新增用户ID
     */
    Long createUser(UserCreateDTO createDTO);

    /**
     * 更新用户信息（含角色关联）
     * @param id 用户ID
     * @param updateDTO 更新参数
     */
    Boolean updateUser(Long id, UserUpdateDTO updateDTO);

    /**
     * 重置用户密码
     * @param id 用户ID
     * @param newPassword 新密码（明文，服务端加密）
     */
    Boolean resetPassword(Long id, String newPassword);

    /**
     * 批量修改用户状态
     * @param ids 用户ID列表
     * @param status 目标状态（0-禁用，1-启用）
     */
    Boolean batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 删除用户（含角色关联解除）
     * @param id 用户ID
     */
    Boolean deleteUser(Long id);
}
