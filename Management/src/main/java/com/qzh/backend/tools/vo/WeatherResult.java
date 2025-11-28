package com.qzh.backend.tools.vo;

import lombok.Data;

import java.util.List;

@Data
public class WeatherResult {
    private Location location; // 地理位置信息
    private Now now; // 实时天气
    private List<WeatherIndex> indexes; // 生活指数
    private List<WeatherAlert> alerts; // 天气预警
    private List<Forecast> forecasts; // 未来几天预报（核心目标）
    private List<HourlyForecast> forecast_hours; // 逐小时预报
}