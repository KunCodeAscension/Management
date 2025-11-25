package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.InventoryDetailQueryDTO;
import com.qzh.backend.model.entity.InventoryDetail;
import com.qzh.backend.model.vo.InventoryDetailVO;

public interface InventoryDetailService extends IService<InventoryDetail> {

    Page<InventoryDetailVO> listInventoryDetailsVO(InventoryDetailQueryDTO queryDTO);

    InventoryDetailVO getInventoryDetailVOById(Long id);
}