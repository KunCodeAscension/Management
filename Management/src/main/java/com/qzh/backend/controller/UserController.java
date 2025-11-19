package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.user.*;
import com.qzh.backend.model.vo.UserVO;
import com.qzh.backend.service.UserService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public BaseResponse<Page<UserVO>> getUserList(@Valid UserQueryDTO queryDTO) {
        Page<UserVO> userPage = userService.getUserPage(queryDTO);
        return ResultUtils.success(userPage);
    }

    @GetMapping("/{id}")
    public BaseResponse<UserVO> getUserDetail(@PathVariable Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.getUserDetailById(id);
        return ResultUtils.success(userVO);
    }

    @PostMapping
    public BaseResponse<Long> createUser(@Valid @RequestBody UserCreateDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Long userId = userService.createUser(dto);
        return ResultUtils.success(userId);
    }

    @PutMapping("/{id}")
    public BaseResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.updateUser(id, dto);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"用户信息更新出错");
        return ResultUtils.success(null);
    }

    @PostMapping("/{id}/reset-password")
    public BaseResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.resetPassword(id, dto.getNewPassword());
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"用户密码更新出错");
        return ResultUtils.success(null);
    }

    @PostMapping("/batch-status")
    public BaseResponse<Void> batchStatus(@Valid @RequestBody BatchStatusDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.batchUpdateStatus(dto.getIds(), dto.getStatus());
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"批量更新用户状态失败");
        return ResultUtils.success(null);
    }

    @DeleteMapping("{id}")
    public BaseResponse<Void> deleteUser(@PathVariable Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.deleteUser(id);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"删除用户出错");
        return ResultUtils.success(null);
    }

}
