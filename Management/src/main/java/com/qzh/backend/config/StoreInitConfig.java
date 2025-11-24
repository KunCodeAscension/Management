package com.qzh.backend.config;

import com.qzh.backend.model.entity.Store;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 门店初始化配置绑定类
 * 直接绑定到一个 Store 对象上
 */
@Data
@Component
@ConfigurationProperties(prefix = "store.init")
public class StoreInitConfig {

    private String storeName;
    private Long managerId;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;


    public Store toStore() {
        Store store = new Store();
        store.setStoreName(this.storeName);
        store.setManagerId(this.managerId);
        store.setContactName(this.contactName);
        store.setContactPhone(this.contactPhone);
        store.setContactEmail(this.contactEmail);
        store.setAddress(this.address);
        store.setLongitude(this.longitude);
        store.setLatitude(this.latitude);
        return store;
    }
}