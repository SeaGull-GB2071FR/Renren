package com.msb.mall.ware.service.impl;

import com.msb.common.dto.WareHasStockDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.ware.dao.WareSkuDao;
import com.msb.mall.ware.entity.WareSkuEntity;
import com.msb.mall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据skuId集合批量查询是否有库存
     * @param skuIds
     * @return
     */
    @Override
    public List<WareHasStockDTO> isHasStock(List<Long> skuIds) {
        List<WareHasStockDTO> list = skuIds.stream().map(skuId -> {
            Long count = baseMapper.getSkuStock(skuId);
            if (null == count){
                count = 0L;
            }
            WareHasStockDTO wareHasStockDTO = new WareHasStockDTO();
            wareHasStockDTO.setSkuId(skuId);
            wareHasStockDTO.setHasStock(count > 0);
            return wareHasStockDTO;
        }).collect(Collectors.toList());

        return list;
    }

}