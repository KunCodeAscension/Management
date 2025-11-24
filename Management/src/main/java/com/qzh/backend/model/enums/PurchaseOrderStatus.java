package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PurchaseOrderStatus {

    /**
     * 待发货
     */
    PENDING("待发货",0),

    /**
     * 已发货
     */
    SHIPPED("已发货",1),

    /**
     * 已入库
     */
    STORED("已入库",2);

    private final String text;
    private final int value;

    PurchaseOrderStatus(String text, int value) {
        this.text = text;
        this.value = value;
    }


    public static PurchaseOrderStatus getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PurchaseOrderStatus pictureReviewStatusEnum : PurchaseOrderStatus.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }

}
