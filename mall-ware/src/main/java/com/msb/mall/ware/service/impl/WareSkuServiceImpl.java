package com.msb.mall.ware.service.impl;

import com.msb.common.dto.WareHasStockDTO;
import com.msb.common.exception.NoStockExecption;
import com.msb.mall.ware.vo.OrderItemVo;
import com.msb.mall.ware.vo.WareSkuLockVO;
import lombok.Data;
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
import org.springframework.transaction.annotation.Transactional;


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

    /**
     * 锁定库存的操作
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {
        List<OrderItemVo> items = vo.getItems();
        // 首先找到具有库存的仓库
        List<SkuWareHasStock> collect = items.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(item.getSkuId());
            List<WareSkuEntity> wareSkuEntities = this.baseMapper.listHashStock(item.getSkuId());
            skuWareHasStock.setWareSkuEntities(wareSkuEntities);
            skuWareHasStock.setNum(item.getCount());
            return skuWareHasStock;
        }).collect(Collectors.toList());
        // 尝试锁定库存
        for (SkuWareHasStock skuWareHasStock : collect) {
            Long skuId = skuWareHasStock.getSkuId();
            List<WareSkuEntity> wareSkuEntities = skuWareHasStock.wareSkuEntities;
            if(wareSkuEntities == null && wareSkuEntities.size() == 0){
                // 当前商品没有库存了
                throw new NoStockExecption(skuId);
            }
            // 当前需要锁定的商品的梳理
            // count = 购买的数量
            Integer count = skuWareHasStock.getNum();
            boolean skuStocked = false; // 表示当前SkuId的库存没有锁定完成
            for (WareSkuEntity wareSkuEntity : wareSkuEntities) {
                // 循环获取到对应的 仓库，然后需要锁定库存
                // 获取当前仓库能够锁定的库存数
                Integer canStock = wareSkuEntity.getStock() - wareSkuEntity.getStockLocked();
                if(count <= canStock){
                    // 表示当前的skuId的商品的数量小于等于需要锁定的数量
                    Integer i = this.baseMapper.lockSkuStock(skuId,wareSkuEntity.getWareId(),count);
                    count = 0;
                    skuStocked = true;
                }else{
                    // 需要锁定的库存大于 可以锁定的库存 就按照已有的库存来锁定
                    Integer i = this.baseMapper.lockSkuStock(skuId,wareSkuEntity.getWareId(),canStock);
                    count = count - canStock;
                }
                if(count <= 0 ){
                    // 表示所有的商品都锁定了
                    break;
                }
            }
            if(count > 0){
                // 说明库存没有锁定完
                throw new NoStockExecption(skuId);
            }
            if(!skuStocked){
                // 表示上一个商品的没有锁定库存成功
                throw new NoStockExecption(skuId);
            }
        }
        return true;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        // 购买的数量
        private Integer num;
        private List<WareSkuEntity> wareSkuEntities;
    }

}