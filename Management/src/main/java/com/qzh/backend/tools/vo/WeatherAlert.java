package com.qzh.backend.tools.vo;

import lombok.Data;

@Data
public class WeatherAlert {
    private String type; // 预警类型
    private String level; // 预警等级
    private String title; // 预警标题
    private String desc; // 预警描述
}