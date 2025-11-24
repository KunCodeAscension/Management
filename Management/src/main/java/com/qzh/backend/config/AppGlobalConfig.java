package com.qzh.backend.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 应用全局配置持有者
 * 用于存储在应用启动时加载的、需要全局访问的配置项
 */
@Component
@Data
public class AppGlobalConfig {

    /**
     * 当前门店ID
     */
    private Long currentStoreId;

    /**
     * 当前门店名称
     */
    private String currentStoreName;

    // 你可以在这里添加其他全局配置，例如：
    // private String appVersion;
    // private String apiBaseUrl;
}