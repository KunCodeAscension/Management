package com.qzh.backend.model.dto.store;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StoreUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "门店ID不能为空")
    private Long id;

    /**
     * 门店名称
     */
    @NotNull(message = "门店名称不能为空")
    private String storeName;

    /**
     * 店长ID
     */
    @NotNull(message = "店长ID不能为空")
    private Long managerId;

    /**
     * 详细地址
     */
    @NotNull(message = "详细地址不能为空")
    private String address;

    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

}
