package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;

public enum ProductStatus {

    PUTON("上架", 1),
    TAKEDOWN("下架", 0);

    private final String text;
    private final int value;

    ProductStatus(String text, int value) {
        this.text = text;
        this.value = value;
    }


    public static ProductStatus getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ProductStatus pictureReviewStatusEnum : ProductStatus.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }

}
