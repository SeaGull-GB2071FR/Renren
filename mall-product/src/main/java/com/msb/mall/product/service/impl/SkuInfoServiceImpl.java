package com.msb.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.msb.common.utils.R;
import com.msb.mall.product.dao.AttrGroupDao;
import com.msb.mall.product.entity.SkuImagesEntity;
import com.msb.mall.product.entity.SpuInfoDescEntity;
import com.msb.mall.product.feign.SeckillFeignService;
import com.msb.mall.product.service.*;
import com.msb.mall.product.vo.SeckillVo;
import com.msb.mall.product.vo.SkuItemSaleAttrVo;
import com.msb.mall.product.vo.SpuItemGroupAttrVo;
import com.msb.mall.product.vo.SpuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.SkuInfoDao;
import com.msb.mall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private ThreadPoolExecutor myThreadPoolExecutor;

    @Autowired
    private SeckillFeignService seckillFeignService;


    /**
     * SKU 信息检索的方法
     * 类别
     * 品牌
     * 价格区间
     * 检索的关键字
     * 分页查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        // 检索关键字
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        // 分类
        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }
        // 品牌
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        // 价格区间
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                // 如果max=0那么我们也不需要加这个条件
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal(0)) == 1) {
                    // 说明 max > 0
                    wrapper.le("price", max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoList;
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId);
        skuInfoList = this.list(wrapper);
        return skuInfoList;
    }

    @Override
    public SpuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SpuItemVO vo = new SpuItemVO();
        // 1.sku的基本信息 pms_sku_info
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
            vo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, myThreadPoolExecutor);
//        // 获取对应的SPUID
//        Long spuId = skuInfoEntity.getSpuId();
//        // 获取对应的CatalogId 类别编号
//        Long catalogId = skuInfoEntity.getCatalogId();
        // 2.sku的图片信息pms_sku_images
        CompletableFuture<Void> imgsFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            vo.setImages(images);
        }, myThreadPoolExecutor);
        // 3.获取spu中的销售属性的组合
        CompletableFuture<Void> saleFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSkuSaleAttrValueBySpuId(res.getSpuId());
            vo.setSaleAttrs(saleAttrs);
        }, myThreadPoolExecutor);

        // 4.获取SPU的介绍
        CompletableFuture<Void> descFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            vo.setDesc(spuInfoDescEntity);
        }, myThreadPoolExecutor);

        // 5.获取SPU的规格参数
        CompletableFuture<Void> baseAttrFuture = skuInfoEntityCompletableFuture.thenAcceptAsync((res) -> {
            List<SpuItemGroupAttrVo> groupAttrVo = attrGroupService.getAttrgroupWithSpuId(res.getCatalogId(), res.getSpuId());
            vo.setBaseAttrs(groupAttrVo);

        }, myThreadPoolExecutor);

        CompletableFuture<Void> SeckillFuture = CompletableFuture.runAsync(() -> {
            // 秒杀信息
            R r = seckillFeignService.getSeckillSessionBySkuId(skuId);
            if (r.getCode() == 0) {
                String seckillInfo = JSON.toJSONString(r.get("seckillInfo"));
                SeckillVo seckillVo = JSON.parseObject(seckillInfo, SeckillVo.class);
                vo.setSeckillVO(seckillVo);
            }
        }, myThreadPoolExecutor);

        CompletableFuture.allOf(imgsFuture, descFuture, baseAttrFuture).get();
        return vo;

    }

}