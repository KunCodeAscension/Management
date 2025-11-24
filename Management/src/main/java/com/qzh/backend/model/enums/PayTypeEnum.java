package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PayTypeEnum {

    /**
     * 支付宝
     */
    ALIPAY("支付宝", 0),

    /**
     * 微信支付
     */
    WECHAT_PAY("微信", 1),

    /**
     * 其他支付方式
     */
    OTHER("其他", 2);

    private final String text;      // 用于前端显示的友好名称
    private final Integer value;     // 用于数据库存储的字符串值

    PayTypeEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public static PayTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PayTypeEnum payTypeEnum : PayTypeEnum.values()) {
            if (payTypeEnum.value.equals(value)) {
                return payTypeEnum;
            }
        }
        return null;
    }
}