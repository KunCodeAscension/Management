package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum SaleOrderStatusEnum {

    /**
     * 待发货
     */
    PENDING("待发货",0),

    /**
     * 已发货
     */
    SHIPPED("已发货",1),

    /**
     * 已完成
     */
    OVERY("已完成",2);

    private final String text;
    private final int value;

    SaleOrderStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }


    public static SaleOrderStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SaleOrderStatusEnum pictureReviewStatusEnum : SaleOrderStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }

}
