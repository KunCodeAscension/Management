package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.user.*;
import com.qzh.backend.model.vo.UserVO;
import com.qzh.backend.service.UserService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.qzh.backend.constants.Interface.UserInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.USER_MODULE;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    private final GetLoginUserUtil getLoginUserUtil;

    @GetMapping("/list")
//    @AuthCheck(interfaceName = USER_LIST_GET)
    public BaseResponse<Page<UserVO>> getUserList(@Valid UserQueryDTO queryDTO) {
        Page<UserVO> userPage = userService.getUserPage(queryDTO);
        return ResultUtils.success(userPage);
    }

    @GetMapping("/{id}")
//    @AuthCheck(interfaceName = USER_DETAIL_GET)
    public BaseResponse<UserVO> getUserDetail(@PathVariable Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        UserVO userVO = userService.getUserDetailById(id);
        return ResultUtils.success(userVO);
    }

    @PostMapping
//    @AuthCheck(interfaceName = USER_CREATE_POST)
    @LogInfoRecord(SystemModule = USER_MODULE + ":" + USER_CREATE_POST)
    public BaseResponse<Long> createUser(@Valid @RequestBody UserCreateDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Long userId = userService.createUser(dto);
        return ResultUtils.success(userId);
    }

    @PutMapping("/{id}")
//    @AuthCheck(interfaceName = USER_UPDATE_PUT)
    @LogInfoRecord(SystemModule = USER_MODULE + ":" + USER_UPDATE_PUT)
    public BaseResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto,HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.updateUser(id, dto,request);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"用户信息更新出错");
        return ResultUtils.success(null);
    }

    @PostMapping("/{id}/reset-password")
//    @AuthCheck(interfaceName = USER_RESET_PASSWORD_POST)
    @LogInfoRecord(SystemModule = USER_MODULE + ":" + USER_RESET_PASSWORD_POST)
    public BaseResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.resetPassword(id, dto.getNewPassword());
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"用户密码更新出错");
        return ResultUtils.success(null);
    }

    @PostMapping("/batch-status")
//    @AuthCheck(interfaceName = USER_BATCH_STATUS_POST)
    @LogInfoRecord(SystemModule = USER_MODULE + ":" + USER_BATCH_STATUS_POST)
    public BaseResponse<Void> batchStatus(@Valid @RequestBody BatchStatusDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.batchUpdateStatus(dto.getIds(), dto.getStatus());
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"批量更新用户状态失败");
        return ResultUtils.success(null);
    }

    @DeleteMapping("{id}")
//    @AuthCheck(interfaceName = USER_DELETE_DELETE)
    @LogInfoRecord(SystemModule = USER_MODULE + ":" + USER_DELETE_DELETE)
    public BaseResponse<Void> deleteUser(@PathVariable Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Boolean b = userService.deleteUser(id);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"删除用户出错");
        return ResultUtils.success(null);
    }

    @PostMapping("login")
    public BaseResponse<Void> login(@RequestBody @Valid UserLoginDTO dto, HttpServletRequest request){
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        userService.login(dto,request);
        return ResultUtils.success(null);
    }

    @PostMapping("register")
    public BaseResponse<Void> register(@RequestBody @Valid UserRegisterDTO dto){
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        userService.register(dto);
        return ResultUtils.success(null);
    }

    /**
     * 获取当前登录的用户信息
     */
    @GetMapping("getLoginUser")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request){
        return ResultUtils.success(UserVO.toUserVO(getLoginUserUtil.getLoginUser(request)));
    }

}
