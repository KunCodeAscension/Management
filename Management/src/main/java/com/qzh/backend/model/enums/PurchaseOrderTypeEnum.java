package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PurchaseOrderTypeEnum {

    /**
     * 手动发起
     */
    MANUAL("手动发起", 0),

    /**
     * 阈值触发
     */
    THRESHOLD("阈值触发", 1);

    private final String text;
    private final int value;

    PurchaseOrderTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PurchaseOrderTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PurchaseOrderTypeEnum orderTypeEnum : PurchaseOrderTypeEnum.values()) {
            if (orderTypeEnum.value == value) {
                return orderTypeEnum;
            }
        }
        return null;
    }
}