package com.msb.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.dto.WareHasStockDTO;
import com.msb.common.utils.PageUtils;
import com.msb.mall.ware.entity.WareSkuEntity;
import com.msb.mall.ware.vo.WareSkuLockVO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-11-19 20:25:53
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<WareHasStockDTO> isHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVO vo);
}

