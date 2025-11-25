package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum InventoryDetailTypeEnum {

    TAKEIN("入库", 1),
    TAKEOUT("出库", 0);

    private final String text;
    private final int value;

    InventoryDetailTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }


    public static InventoryDetailTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (InventoryDetailTypeEnum pictureReviewStatusEnum : InventoryDetailTypeEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }

}
