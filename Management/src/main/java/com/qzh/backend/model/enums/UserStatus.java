package com.qzh.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum UserStatus {

    NORMAL("通过", 1),
    Disable("停用", 0);

    private final String text;
    private final int value;

    UserStatus(String text, int value) {
        this.text = text;
        this.value = value;
    }


    public static UserStatus getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserStatus pictureReviewStatusEnum : UserStatus.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }

}
