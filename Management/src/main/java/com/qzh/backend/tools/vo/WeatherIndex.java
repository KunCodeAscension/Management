package com.qzh.backend.tools.vo;

import lombok.Data;

@Data
public class WeatherIndex {
    private String name; // 指数名称
    private String brief; // 简要建议
    private String detail; // 详细说明
}