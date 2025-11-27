package com.qzh.backend.model.dto.product;

import lombok.Data;

import java.io.Serializable;

@Data
public class SaleReturnCreateDTO implements Serializable {

    /**
     * 销售ID
     */
    private Long saleOrderId;

    /**
     * 退款原因
     */
    private String reason;

}
