package com.qzh.backend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qzh.backend.config.StoreInitConfig;
import com.qzh.backend.mapper.StoreMapper;
import com.qzh.backend.model.entity.Store;
import com.qzh.backend.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 单门店初始化执行器：确保系统中只有一条门店信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreInitRunner implements ApplicationRunner {

    private final StoreInitConfig storeInitConfig;

    private final StoreService storeService;

    private final AppGlobalConfig appGlobalConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===== 开始执行单门店初始化任务 =====");
        Store existingStore = storeService.getOne(new QueryWrapper<>());
        Store configStore = storeInitConfig.toStore();
        if (existingStore == null) {
            storeService.save(configStore);
        } else {
            configStore.setId(existingStore.getId());
            storeService.updateById(configStore);
        }
        appGlobalConfig.setCurrentStoreId(configStore.getId());
        appGlobalConfig.setCurrentStoreName(configStore.getStoreName());
        log.info("===== 单门店初始化任务执行完成 =====");
        log.info("门店ID：{}，门店名：{}", appGlobalConfig.getCurrentStoreId(),appGlobalConfig.getCurrentStoreName());
    }
}