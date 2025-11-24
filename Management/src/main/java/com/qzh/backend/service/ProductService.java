package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.ProductAddDTO;
import com.qzh.backend.model.dto.product.ProductQueryDTO;
import com.qzh.backend.model.dto.product.ProductUpdateDTO;
import com.qzh.backend.model.entity.Product;
import jakarta.servlet.http.HttpServletRequest;

public interface ProductService extends IService<Product> {

    /**
     * 新增商品
     * @param productAddDTO 商品新增DTO
     * @return 新增商品的ID
     */
    Long addProduct(ProductAddDTO productAddDTO, HttpServletRequest request);

    /**
     * 更新商品
     * @param productUpdateDTO 商品更新DTO
     * @return 是否更新成功
     */
    boolean updateProduct(ProductUpdateDTO productUpdateDTO,HttpServletRequest request);

    /**
     * 分页查询商品
     *
     * @param productQueryDTO 查询条件
     * @return 分页结果
     */
    Page<Product> listProductsByPage(ProductQueryDTO productQueryDTO);

    /**
     * 分页商品（供应商）
     *
     * @param productQueryDTO 查询条件
     * @return 分页结果
     */
    Page<Product> listProductsByPageOwn(ProductQueryDTO productQueryDTO,HttpServletRequest request);

}
