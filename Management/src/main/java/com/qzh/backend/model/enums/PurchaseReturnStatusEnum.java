package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PurchaseReturnStatusEnum {

    COMPLETED("已完成", 1),
    UNFINISHED("未完成", 0);

    private final String text;
    private final int value;

    PurchaseReturnStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }


    public static PurchaseReturnStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PurchaseReturnStatusEnum pictureReviewStatusEnum : PurchaseReturnStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }

}
