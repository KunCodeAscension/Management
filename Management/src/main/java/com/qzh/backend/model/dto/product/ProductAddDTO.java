package com.qzh.backend.model.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "商品名称不能为空")
    private String name;

    private String description;

    private String url;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private Integer status = 1;

}