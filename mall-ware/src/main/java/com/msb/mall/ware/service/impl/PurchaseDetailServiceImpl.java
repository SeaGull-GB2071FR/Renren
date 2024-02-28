package com.msb.mall.ware.service.impl;

import com.msb.common.utils.R;
import com.msb.mall.ware.dao.WareSkuDao;
import com.msb.mall.ware.entity.WareSkuEntity;
import com.msb.mall.ware.feign.ProductFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.ware.dao.PurchaseDetailDao;
import com.msb.mall.ware.entity.PurchaseDetailEntity;
import com.msb.mall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                new QueryWrapper<PurchaseDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {

        return null;
    }

    /**
     * 入库操作
     * @param skuId 商品编号
     * @param wareId 仓库编号
     * @param skuNum  采购商品的数量
     */
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断是否有改商品和仓库的入库记录
        List<WareSkuEntity> list = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(list == null || list.size() == 0){
            // 如果没有就新增商品库存记录
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setWareId(wareId);
            entity.setStock(skuNum);
            entity.setStockLocked(0);
            try {
                // 动态的设置商品的名称
                R info = productFeignService.info(skuId); // 通过Feign远程调用商品服务的接口
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if(info.getCode() == 0){
                    entity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }
            wareSkuDao.insert(entity); // 插入商品库存记录
        }else{
            // 如果有就更新库存
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }


    }

}