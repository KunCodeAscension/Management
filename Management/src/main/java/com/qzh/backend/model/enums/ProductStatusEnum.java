package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum ProductStatusEnum {

    PUTON("上架", 1),
    TAKEDOWN("下架", 0);

    private final String text;
    private final int value;

    ProductStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }


    public static ProductStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ProductStatusEnum pictureReviewStatusEnum : ProductStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }

}
