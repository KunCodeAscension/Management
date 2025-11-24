package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    /**
     * 待支付
     */
    PENDING_PAYMENT("待支付", 0),

    /**
     * 已支付
     */
    PAID("已支付", 1),

    /**
     * 已取消
     */
    CANCELLED("已取消", 2);

    private final String text;
    private final int value;

    OrderStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取对应的枚举
     *
     * @param value 数据库中存储的数值
     * @return 对应的枚举对象，若未找到或输入为空则返回 null
     */
    public static OrderStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (OrderStatusEnum statusEnum : OrderStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}