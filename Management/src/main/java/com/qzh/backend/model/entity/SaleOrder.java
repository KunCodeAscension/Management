package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("sys_sale_order")
public class SaleOrder implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long storeId;

    private Long userId;

    private Long productId;

    private String productName;

    private String productUrl;

    private String productDescription;

    private BigDecimal productPrice;

    private Integer productQuantity;

    private BigDecimal totalAmount;

    private Long warehouseId;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Long createBy;
}