package com.qzh.backend.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.model.entity.Product;
import com.qzh.backend.model.entity.SaleOrder;
import com.qzh.backend.service.ProductService;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.tools.query.SaleOrderQueryDTO;
import com.qzh.backend.tools.vo.Forecast;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AITools {

    private final ProductService productService;

    private final SaleOrderService saleOrderService;

    private final WeatherApiClient weatherApiClient;

    @Tool(description = "获取供应商再售商品列表、门店销售数据、未来几天天气的聚合信息，用于预估热销商品")
    public DataAggregation queryDataForSalesForecast(
            @ToolParam(description = "销售订单查询条件（可选，为空返回所有订单）", required = false) SaleOrderQueryDTO saleOrderQueryDTO) throws Exception {
        // 查询供应商在售商品
        List<Product> products = productService.list(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        // 查询销售订单数据
        List<SaleOrder> saleOrders;
        if (saleOrderQueryDTO == null) {
            saleOrders = saleOrderService.list();
        } else {
            QueryWrapper<SaleOrder> queryWrapper = SaleOrderQueryDTO.getQueryWrapper(saleOrderQueryDTO);
            saleOrders = saleOrderService.list(queryWrapper);
        }
        // 查询天气数据
        List<Forecast> forecasts = weatherApiClient.getForecastsByDistrictId("110105");
        // 返回聚合结果
        return new DataAggregation(products, saleOrders, forecasts);
    }

    @Data
    @AllArgsConstructor
    public static class DataAggregation {
        private List<Product> products;
        private List<SaleOrder> saleOrders;
        private List<Forecast> forecasts;
    }
}
