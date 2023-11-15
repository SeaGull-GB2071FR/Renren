package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

