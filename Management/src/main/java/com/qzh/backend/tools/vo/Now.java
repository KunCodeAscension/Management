package com.qzh.backend.tools.vo;

import lombok.Data;

@Data
public class Now {
    private String text; // 天气状况文字
    private Integer temp; // 实时气温
    private Integer feels_like; // 体感温度
    private Integer rh; // 相对湿度（%）
    private String wind_class; // 风力等级
    private String wind_dir; // 风向
    // 其他实时字段可根据需求添加
}