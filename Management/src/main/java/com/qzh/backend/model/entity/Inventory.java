package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 库存表实体
 * 对应数据库表：sys_inventory
 */
@Data
@TableName("sys_inventory")
public class Inventory implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 库存ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品描述
     */
    private String productDescription;

    /**
     * 商品图片
     */
    private String productUrl;

    /**
     * 出售单价
     */
    private BigDecimal productPrice;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 预警阈值
     */
    private Integer warningThreshold;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}