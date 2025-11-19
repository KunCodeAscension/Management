package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.UserMapper;
import com.qzh.backend.model.dto.user.UserCreateDTO;
import com.qzh.backend.model.dto.user.UserQueryDTO;
import com.qzh.backend.model.dto.user.UserUpdateDTO;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.entity.UserRelatedRole;
import com.qzh.backend.model.enums.UserStatus;
import com.qzh.backend.model.vo.UserVO;
import com.qzh.backend.service.RoleService;
import com.qzh.backend.service.UserRelatedRoleService;
import com.qzh.backend.service.UserService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRelatedRoleService userRelatedRoleService;

    private final RoleService roleService;

    @Override
    public Page<UserVO> getUserPage(UserQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询 User列表 并且把User 转为 UserVO
        Page<User> page = this.page(new Page<>(current, size), UserQueryDTO.getQueryWrapper(queryDTO));
        Page<UserVO> userVOPage = new Page<>(current, size, page.getTotal());
        List<UserVO> userVOList = UserVO.toUserVOList(page.getRecords());
        // 查询UserVO 下关联的 RoleName
        if (!CollectionUtils.isEmpty(userVOList)) {
            // 提取当前页所有用户ID
            List<Long> userIds = userVOList.stream()
                    .map(UserVO::getId)
                    .collect(Collectors.toList());
            // 批量查询用户-角色关联关系
            List<UserRelatedRole> userRoleRelations = userRelatedRoleService.list(
                    new LambdaQueryWrapper<UserRelatedRole>()
                            .in(UserRelatedRole::getUserId, userIds)
            );
            if (!CollectionUtils.isEmpty(userRoleRelations)) {
                // 提取所有角色ID，批量查询角色信息 distinct去重
                List<Long> roleIds = userRoleRelations.stream()
                        .map(UserRelatedRole::getRoleId)
                        .distinct()
                        .collect(Collectors.toList());

                // 构建 角色ID -> 角色名称 的映射
                Map<Long, String> roleIdToNameMap = roleService.list(
                                new LambdaQueryWrapper<Role>()
                                        .in(Role::getId, roleIds)
                        ).stream()
                        .collect(Collectors.toMap(
                                Role::getId,
                                Role::getRoleName,
                                (oldVal, newVal) -> oldVal
                        ));

                // 构建 用户ID -> 角色名称列表 的映射
                Map<Long, List<String>> userIdToRoleNamesMap = userRoleRelations.stream()
                        .collect(Collectors.groupingBy(
                                UserRelatedRole::getUserId, // 按用户ID分组
                                Collectors.mapping(
                                        relation -> roleIdToNameMap.get(relation.getRoleId()), // 转换为角色名称
                                        Collectors.filtering(Objects::nonNull, Collectors.toList()) // 过滤无效角色
                                )
                        ));

                // 给每个UserVO设置角色名称列表
                userVOList.forEach(userVO -> {
                    List<String> roleNames = userIdToRoleNamesMap.getOrDefault(userVO.getId(), Collections.emptyList());
                    userVO.setRoleNames(roleNames);
                });
            } else {
                // 无角色关联，设置空列表
                userVOList.forEach(vo -> vo.setRoleNames(Collections.emptyList()));
            }
        }
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    @Override
    public UserVO getUserDetailById(Long id) {
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR);
        UserVO userVO = UserVO.toUserVO(user);
        // 查询该用户的角色关联关系
        List<UserRelatedRole> userRoleRelations = userRelatedRoleService.list(
                new LambdaQueryWrapper<UserRelatedRole>()
                        .eq(UserRelatedRole::getUserId, id)
        );
        // 填充角色名称列表
        if (!CollectionUtils.isEmpty(userRoleRelations)) {
            // 提取角色ID列表
            List<Long> roleIds = userRoleRelations.stream()
                    .map(UserRelatedRole::getRoleId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询角色信息，构建 角色ID->角色名称 映射
            Map<Long, String> roleIdToNameMap = roleService.list(
                            new LambdaQueryWrapper<Role>()
                                    .in(Role::getId, roleIds)
                    ).stream()
                    .collect(Collectors.toMap(
                            Role::getId,
                            Role::getRoleName,
                            (oldVal, newVal) -> oldVal
                    ));
            List<String> roleNames = roleIds.stream()
                    .map(roleIdToNameMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            userVO.setRoleNames(roleNames);
        } else {
            userVO.setRoleNames(Collections.emptyList());
        }
        return userVO;
    }

    @Override
    public Long createUser(UserCreateDTO createDTO) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);
        // userAccount 唯一性判断
        boolean accountExists = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, createDTO.getUserAccount())) > 0;
        ThrowUtils.throwIf(accountExists, ErrorCode.PARAMS_ERROR, "用户账号已存在");
        // 密码加密（使用Spring Security的PasswordEncoder，避免明文存储）
        String encryptedPassword = getEncryptPassword(createDTO.getUserPassword());
        User user = new User();
        user.setUserAccount(createDTO.getUserAccount());
        user.setUserPassword(encryptedPassword);
        user.setUserName(createDTO.getUserName());
        user.setPhone(createDTO.getPhone());
        user.setStatus(UserStatus.getEnumByValue(createDTO.getStatus()).getValue());
        boolean success = this.save(user);
        ThrowUtils.throwIf(!success,ErrorCode.SYSTEM_ERROR);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUser(Long id, UserUpdateDTO updateDTO) {
        ThrowUtils.throwIf(updateDTO == null, ErrorCode.PARAMS_ERROR);
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        user.setUserName(updateDTO.getUserName());
        user.setPhone(updateDTO.getPhone());
        user.setStatus(UserStatus.getEnumByValue(updateDTO.getStatus()).getValue());
        // 4. 处理角色关联（先删后加，覆盖式更新）
        List<Long> newRoleIds = updateDTO.getRoleIds();
        boolean remove;
        if (!CollectionUtils.isEmpty(newRoleIds)) {
            // 4.1 删除该用户原有所有角色关联（sys_user_role表）
            remove = userRelatedRoleService.remove(
                    new LambdaQueryWrapper<UserRelatedRole>()
                            .eq(UserRelatedRole::getUserId, id)
            );

            // 4.2 批量构建新的角色关联实体
            List<UserRelatedRole> userRoleList = newRoleIds.stream()
                    .map(roleId -> {
                        UserRelatedRole userRelatedRole = new UserRelatedRole();
                        userRelatedRole.setUserId(id);
                        userRelatedRole.setRoleId(roleId);
                        // TODO 创建人ID
                        // userRelatedRole.setCreateBy(getCurrentUserId());
                        return userRelatedRole;
                    })
                    .collect(Collectors.toList());
            boolean saveBatch = userRelatedRoleService.saveBatch(userRoleList);
            remove = saveBatch && remove;
        } else {
            // 若传入角色ID列表为空，删除该用户所有角色关联（可选：根据业务需求决定是否保留）
            remove = userRelatedRoleService.remove(
                    new LambdaQueryWrapper<UserRelatedRole>()
                            .eq(UserRelatedRole::getUserId, id)
            );
        }
        boolean updateById = this.updateById(user);
        return updateById && remove;
    }

    @Override
    public Boolean resetPassword(Long id, String newPassword) {
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        // 密码校验在controller层
        String encryptPassword = getEncryptPassword(newPassword);
        user.setUserPassword(encryptPassword);
        return this.updateById(user);
    }

    @Override
    public Boolean batchUpdateStatus(List<Long> ids, Integer status) {
        // 校验用户ID是否存在（可选：避免更新不存在的用户ID，增强健壮性）
        List<Long> existingUserIds = this.listByIds(ids).stream()
                .map(User::getId)
                .toList();
        // 对比传入的ID和实际存在的ID，找出不存在的ID
        List<Long> notExistIds = ids.stream()
                .filter(id -> !existingUserIds.contains(id))
                .toList();
        ThrowUtils.throwIf(!notExistIds.isEmpty(), ErrorCode.NOT_FOUND_ERROR,
                "以下用户ID不存在：" + notExistIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        // 批量更新用户状态和更新时间
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<User>()
                .in(User::getId, ids)
                .set(User::getStatus, status);
        return this.update(updateWrapper);
    }

    @Override
    public Boolean deleteUser(Long id) {
         return this.removeById(id);
         // TODO 删除角色 权限 关联表 数据
    }

    public String getEncryptPassword(String userPassword) {
        final String SALT = "qzh";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}
