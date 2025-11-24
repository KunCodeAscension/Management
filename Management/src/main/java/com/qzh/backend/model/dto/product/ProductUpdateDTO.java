package com.qzh.backend.model.dto.product;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductUpdateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long id;

    @NotNull(message = "商品名称不能为空")
    private String name;

    private String description;

    private String url;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    @NotNull(message = "商品状态不能为空")
    private Integer status;
}