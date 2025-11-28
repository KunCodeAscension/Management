package com.qzh.backend.tools.vo;

import lombok.Data;

@Data
public class Location {
    private String country; // 国家
    private String province; // 省份
    private String city; // 城市
    private String name; // 区县名称
    private String id; // 区县ID
}