package com.qzh.backend.tools.vo;

import lombok.Data;

@Data
public class HourlyForecast {
    private String text; // 天气状况
    private Integer temp_fc; // 预报气温
    private String wind_class; // 风力
    private String wind_dir; // 风向
    private String data_time; // 预报时间
    // 其他逐小时字段可根据需求添加
}