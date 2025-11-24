package com.qzh.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.store.StoreUpdateDTO;
import com.qzh.backend.model.entity.Store;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.service.StoreService;
import com.qzh.backend.service.UserService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RequiredArgsConstructor
@RestController
@RequestMapping("store")
public class StoreController {

    private final StoreService storeService;

    private final UserService userService;

    @GetMapping
    public BaseResponse<Store> getStore() {
        Store store = storeService.getOne(new QueryWrapper<>());
        return ResultUtils.success(store);
    }

    @PutMapping
    public BaseResponse<Void> updateStore(@RequestBody @Valid StoreUpdateDTO storeUpdateDTO) {
        ThrowUtils.throwIf(storeUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        Long userId = storeUpdateDTO.getManagerId();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"店长账户不存在");
        }
        Store store = storeService.getById(storeUpdateDTO.getId());
        if (store == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"门店不存在");
        }
        store.setStoreName(storeUpdateDTO.getStoreName());
        store.setAddress(storeUpdateDTO.getAddress());
        store.setManagerId(userId);
        store.setContactName(user.getUserName());
        store.setContactPhone(user.getPhone());
        store.setContactEmail(user.getEmail());
        store.setLatitude(storeUpdateDTO.getLatitude());
        store.setLongitude(storeUpdateDTO.getLongitude());
        boolean b = storeService.updateById(store);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"更新门店信息失败");
        return ResultUtils.success(null);
    }

}
