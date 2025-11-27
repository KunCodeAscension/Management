package com.qzh.backend.model.dto.product;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ProductUpdateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long id;

    @NotNull(message = "商品名称不能为空")
    private String name;

    private String description;

    private String url;

    @NotNull(message = "商品状态不能为空")
    private Integer status;
}