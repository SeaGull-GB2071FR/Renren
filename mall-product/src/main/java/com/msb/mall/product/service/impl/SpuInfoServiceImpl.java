package com.msb.mall.product.service.impl;

import com.aliyun.oss.common.utils.StringUtils;
import com.msb.common.dto.SkuReductionDTO;
import com.msb.common.dto.SpuBoundsDTO;
import com.msb.mall.product.entity.*;
import com.msb.mall.product.feign.CouponFeignService;
import com.msb.mall.product.service.*;
import com.msb.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;


    /**
     * 发布商品-保存商品信息
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVo vo) {
        // 1.保存spu的基本信息pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setPublishStatus(0);
        this.save(spuInfoEntity);
        // 2.保存spu详情信息 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        List<String> decript = vo.getDecript();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.save(spuInfoDescEntity);
        // 3.保存图片集信息 pms_spu_images
        List<String> images = vo.getImages();
        List<SpuImagesEntity> spuImagesList = images.stream().map(item -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfoEntity.getId());
            spuImagesEntity.setImgUrl(item);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesList);
        // 4.保存规格参数信息 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> paveList = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuInfoEntity.getId()); // 关联商品编号
            productAttrValueEntity.setAttrId(item.getAttrId());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            // 获取属性名称
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());

            return productAttrValueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveBatch(paveList);
        // 5.保存当前spu对应的sku信息
        // 5.1保存sku基本信息pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (null != skus && skus.size() > 0){
            skus.forEach(item -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                List<Images> imagesList = item.getImages();
                imagesList.forEach(image -> {
                    if (1 == image.getDefaultImg()) {
                        skuInfoEntity.setSkuDefaultImg(image.getImgUrl());
                    }
                });
                skuInfoEntity.setSaleCount(0L);
                skuInfoService.save(skuInfoEntity);

                // 5.2保存sku图片信息pms_sku_image
                List<SkuImagesEntity> imgList = imagesList.stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imgList);

                // 5.3保存满减信息，折扣，会员价 mall_sms:sms_sku_ladder,sms_sku_full_reduction,sms_member_price
                SkuReductionDTO skuReductionDTO = new SkuReductionDTO();
                BeanUtils.copyProperties(item,skuReductionDTO);
                skuReductionDTO.setSkuId(skuInfoEntity.getSkuId());
                couponFeignService.saveFullReductionInfo(skuReductionDTO);

                // 5.4sku的销售属性信息pms_sku_sale_attr_value
                List<Attr> skuAttrs = item.getAttr();
                List<SkuSaleAttrValueEntity> ssaveList = skuAttrs.stream().map(skuAttr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(skuAttr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(ssaveList);
            });
        }

        // 6.保存spu的积分信息：mall_sms:sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundsDTO spuBoundsDTO = new SpuBoundsDTO();
        BeanUtils.copyProperties(bounds,spuBoundsDTO);
        spuBoundsDTO.setSpuId(spuInfoEntity.getId());
        couponFeignService.saveSpuBounds(spuBoundsDTO);
    }

    /**
     * spu检索
     * 分类
     * 品牌
     * 状态
     * key id,spu_name,spu_description
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isNullOrEmpty(key)){
            wrapper.and(k->{
                k.eq("id",key).or()
                        .like("spu_name",key).or()
                        .like("spu_description",key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isNullOrEmpty(status)){
            wrapper.and(k->k.eq("publish_status",status));
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isNullOrEmpty(brandId)){
            wrapper.and(k->k.eq("brand_id",brandId));
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isNullOrEmpty(catelogId)){
            wrapper.and(k->k.eq("catalog_id",catelogId));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        List<SpuInfoEntity> records = page.getRecords();
        List<SpuInfoVo> spuInfoVoList = records.stream().map(item -> {
            SpuInfoVo spuInfoVo = new SpuInfoVo();
            BeanUtils.copyProperties(item, spuInfoVo);
            // 查询分类名称和品牌名称
            CategoryEntity categrouy = categoryService.getById(spuInfoVo.getCatalogId());
            BrandEntity brand = brandService.getById(spuInfoVo.getBrandId());
            spuInfoVo.setCatalogName(categrouy.getName());
            spuInfoVo.setBrandName(brand.getName());
            return spuInfoVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(spuInfoVoList);
        return pageUtils;
    }


}