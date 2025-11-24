package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 金额订单表实体
 * 对应数据库表：sys_amount_order
 */
@Data
@TableName("sys_amount_order")
public class AmountOrder implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 金额订单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单编号
     */
    private Long orderId;

    /**
     * 类型（0-采购，1-采退，2-销售，3-销退）
     */
    private Integer type;

    /**
     * 付款人ID（门店ID）
     */
    private Long payerId;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 收款人ID（供应商ID）
     */
    private Long payeeId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 状态（0-待支付，1-已支付，2-已取消）
     */
    private Integer status;

    /**
     * 支付方式（alipay-支付宝）
     */
    private Integer payType;

    /**
     * 第三方支付流水号
     */
    private String tradeNo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人ID
     */
    private Long createBy;
}