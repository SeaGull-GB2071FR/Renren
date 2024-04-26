package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.dto.es.SkuESModel;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.SpuInfoEntity;
import com.msb.mall.product.vo.OrderItemSpuInfoVO;
import com.msb.mall.product.vo.SpuInfoVo;

import java.util.List;
import java.util.Map;

/**
 * spu信息
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-11-15 08:56:47
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPageByCondition(Map<String, Object> params);

    void saveSpuInfo(SpuInfoVo spuInfoVo);

    void Up(Long spuId);

    List<SkuESModel.Attrs> getAttrsModel(Long spuId);

    List<OrderItemSpuInfoVO> getOrderItemSpuInfoBySpuId(Long[] spuIds);
}

