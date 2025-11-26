package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum OrderTypeEnum {

    /**
     * 采购
     */
    PURCHASE("采购", 0),

    /**
     * 采退
     */
    PURCHASE_RETURN("采退", 1),

    /**
     * 销售
     */
    SALE("销售", 2),

    /**
     * 销退
     */
    SALE_RETURN("销退", 3),

    /**
     * 调拨转出
     */
    TRANSFER_OUT("调拨转出", 4),

    /**
     * 调拨转入
     */
    TRANSFER_IN("调拨转入", 5);

    private final String text;
    private final int value;

    OrderTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static OrderTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (OrderTypeEnum orderTypeEnum : OrderTypeEnum.values()) {
            if (orderTypeEnum.value == value) {
                return orderTypeEnum;
            }
        }
        return null;
    }
}