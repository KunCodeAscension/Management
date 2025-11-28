package com.qzh.backend.tools.vo;

import lombok.Data;

@Data
public class Forecast {
    private String date; // 日期（yyyy-MM-dd）
    private String week; // 星期
    private Integer high; // 最高气温
    private Integer low; // 最低气温
    private String wc_day; // 白天风力
    private String wc_night; // 夜间风力
    private String wd_day; // 白天风向
    private String wd_night; // 夜间风向
    private String text_day; // 白天天气状况
    private String text_night; // 夜间天气状况
    private Integer aqi; // 空气质量指数（部分返回结果可能包含）
}