package com.qzh.backend.tools.vo;

import lombok.Data;

@Data
public class WeatherResponse {
    private Integer status; // 响应状态（0成功）
    private WeatherResult result; // 核心结果数据
    private String message; // 响应信息
}