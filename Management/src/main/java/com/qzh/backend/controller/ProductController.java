package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.ProductAddDTO;
import com.qzh.backend.model.dto.product.ProductQueryDTO;
import com.qzh.backend.model.dto.product.ProductUpdateDTO;
import com.qzh.backend.model.entity.Product;
import com.qzh.backend.service.ProductService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public BaseResponse<Long> addProduct(@Valid @RequestBody ProductAddDTO productAddDTO, HttpServletRequest request) {
        Long productId = productService.addProduct(productAddDTO,request);
        return ResultUtils.success(productId);
    }

    @PutMapping
    public BaseResponse<Void> updateProduct(@Valid @RequestBody ProductUpdateDTO productUpdateDTO,HttpServletRequest request) {
        boolean result = productService.updateProduct(productUpdateDTO,request);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"商品信息更新失败");
        return ResultUtils.success(null);
    }

    @GetMapping("/{id}")
    public BaseResponse<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return ResultUtils.success(product);
    }

    @GetMapping("/list")
    public BaseResponse<Page<Product>> listProducts(ProductQueryDTO productQueryDTO) {
        Page<Product> productPage = productService.listProductsByPage(productQueryDTO);
        return ResultUtils.success(productPage);
    }

    @GetMapping("/list/own")
    public BaseResponse<Page<Product>> listProductsByOwn(ProductQueryDTO productQueryDTO , HttpServletRequest request) {
        Page<Product> productPage = productService.listProductsByPageOwn(productQueryDTO, request);
        return ResultUtils.success(productPage);
    }

}
