package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.ProductMapper;
import com.qzh.backend.model.dto.product.ProductAddDTO;
import com.qzh.backend.model.dto.product.ProductQueryDTO;
import com.qzh.backend.model.dto.product.ProductUpdateDTO;
import com.qzh.backend.model.entity.Product;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.service.ProductService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final GetLoginUserUtil getLoginUserUtil;

    @Override
    public Long addProduct(ProductAddDTO productAddDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(productAddDTO==null,ErrorCode.PARAMS_ERROR);
        Product product = new Product();
        product.setName(productAddDTO.getName());
        product.setDescription(productAddDTO.getDescription());
        product.setUrl(productAddDTO.getUrl());
        User loginUser = getLoginUserUtil.getLoginUser(request);
        product.setSupplierId(loginUser.getId());
        if(productAddDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "售价不能小于等于0");
        }
        product.setPrice(productAddDTO.getPrice());
        product.setStatus(productAddDTO.getStatus());
        boolean saveResult = this.save(product);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"新增商品错误");
        return product.getId();
    }

    @Override
    public boolean updateProduct(ProductUpdateDTO productUpdateDTO,HttpServletRequest request) {
        ThrowUtils.throwIf(productUpdateDTO==null,ErrorCode.PARAMS_ERROR);
        Product existingProduct = this.getById(productUpdateDTO.getId());
        if (existingProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        User loginUser = getLoginUserUtil.getLoginUser(request);
        if (!loginUser.getId().equals(existingProduct.getSupplierId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"禁止修改他人商品数据");
        }
        existingProduct.setName(productUpdateDTO.getName());
        existingProduct.setDescription(productUpdateDTO.getDescription());
        existingProduct.setUrl(productUpdateDTO.getUrl());
//        existingProduct.setPrice(productUpdateDTO.getPrice());
        existingProduct.setStatus(productUpdateDTO.getStatus());
        return this.updateById(existingProduct);
    }

    @Override
    public Page<Product> listProductsByPage(ProductQueryDTO productQueryDTO) {
        ThrowUtils.throwIf(productQueryDTO==null,ErrorCode.PARAMS_ERROR);
        int current = productQueryDTO.getCurrent();
        int size = productQueryDTO.getSize();
        ThrowUtils.throwIf(size > 20 ,ErrorCode.PARAMS_ERROR);
        Page<Product> page = new Page<>(current, size);
        return this.page(page, ProductQueryDTO.getQueryWrapper(productQueryDTO));
    }

    @Override
    public Page<Product> listProductsByPageOwn(ProductQueryDTO productQueryDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(productQueryDTO==null,ErrorCode.PARAMS_ERROR);
        int current = productQueryDTO.getCurrent();
        int size = productQueryDTO.getSize();
        ThrowUtils.throwIf(size > 20 ,ErrorCode.PARAMS_ERROR);
        // 根据登录用户设置供应商ID
        User loginUser = getLoginUserUtil.getLoginUser(request);
        productQueryDTO.setSupplierId(loginUser.getId());
        Page<Product> page = new Page<>(current, size);
        return this.page(page, ProductQueryDTO.getQueryWrapper(productQueryDTO));
    }
}
