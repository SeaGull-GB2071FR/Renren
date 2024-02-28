package com.msb.mall.product.service.impl;

import com.aliyun.oss.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.common.constant.ProductConstant;
import com.msb.common.dto.SkuReductionDTO;
import com.msb.common.dto.SpuBoundsDTO;
import com.msb.common.dto.WareHasStockDTO;
import com.msb.common.dto.es.SkuESModel;
import com.msb.common.utils.R;
import com.msb.mall.product.entity.*;
import com.msb.mall.product.feign.CouponFeignService;
import com.msb.mall.product.feign.WareFeignService;
import com.msb.mall.product.service.*;
import com.msb.mall.product.vo.*;
import com.msb.mall.product.feign.SearchFeignService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    private SearchFeignService searchFeignService;

    @Autowired
    private WareFeignService wareFeignService;


    /**
     * 发布商品-保存商品信息
     *
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVo vo) {
        // 1.保存spu的基本信息pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setPublishStatus(0);
        this.save(spuInfoEntity);
        // 2.保存spu详情信息 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        List<String> decript = vo.getDecript();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
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
        if (null != skus && skus.size() > 0) {
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
                BeanUtils.copyProperties(item, skuReductionDTO);
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
        BeanUtils.copyProperties(bounds, spuBoundsDTO);
        spuBoundsDTO.setSpuId(spuInfoEntity.getId());
        couponFeignService.saveSpuBounds(spuBoundsDTO);
    }

    /**
     * 实现商品上架--》商品相关数据存储到ElasticSearch中
     * 1.根据SpuID查询出相关的信息
     * 封装到对应的对象中
     * 2.将封装的数据存储到ElasticSearch中--》调用mall-search的远程接口
     * 3.更新SpuID对应的状态--》上架
     * todo
     * @param spuId
     */
    @Override
    public void Up(Long spuId) {
        // 1.根据spuId查询相关的信息 封装到SkuESModel对象中
        // 根据spuID找到对应的SKU信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        // 对应的规格参数  根据spuId来查询规格参数信息
        List<SkuESModel.Attrs> attrsModel = getAttrsModel(spuId);
        // 需要根据所有的skuId获取对应的库存信息---》远程调用

        List<Long> skuIds = skus.stream().map(sku -> {
            return sku.getSkuId();
        }).collect(Collectors.toList());
        Map<Long, Boolean> skusHasStockMap = getSkusHasStock(skuIds);
        // 2.远程调用mall-search的服务，将SukESModel中的数据存储到ES中
        List<SkuESModel> skuESModels = skus.stream().map(item -> {
            SkuESModel model = new SkuESModel();
            // 先实现属性的复制
            BeanUtils.copyProperties(item, model);
            model.setSubTitle(item.getSkuTitle());
            model.setSkuPrice(item.getPrice());

            // hasStock 是否有库存 --》 库存系统查询  一次远程调用获取所有的skuId对应的库存信息
            if (skusHasStockMap == null) {
                model.setHasStock(true);
            } else {
                model.setHasStock(skusHasStockMap.get(item.getSkuId()));
            }
            // hotScore 热度分 --> 默认给0即可
            model.setHotScore(0l);
            // 品牌和类型的名称
            BrandEntity brand = brandService.getById(item.getBrandId());
            CategoryEntity category = categoryService.getById(item.getCatalogId());
            model.setBrandName(brand.getName());
            model.setBrandImg(brand.getLogo());
            model.setCatalogName(category.getName());
            // 需要存储的规格数据
            model.setAttrs(attrsModel);

            return model;
        }).collect(Collectors.toList());
        // 将SkuESModel中的数据存储到ES中
        R r = searchFeignService.productStatusUp(skuESModels);
        // 3.更新SPUID对应的状态
        // 根据对应的状态更新商品的状态
//        log.info("----->ES操作完成：{}" ,r.getCode());
        System.out.println("-------------->" + r.getCode());
        if (r.getCode() == 0) {
//            // 远程调用成功  更新商品的状态为 上架
//            baseMapper.updateSpuStatusUp(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
            // 远程调用成功   修该spuId对应商品状态为上架
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setPublishStatus(ProductConstant.StatusEnum.SPU_UP.getCode());
            this.update(spuInfoEntity,new UpdateWrapper<SpuInfoEntity>()
                    .eq("id",spuId));
        } else {
            // 远程调用失败
        }
    }

    private Map<Long, Boolean> getSkusHasStock(List<Long> skuIds) {
        if (skuIds == null || skuIds.size() == 0) {
            return null;
        }
        Map<Long, Boolean> hasStockMap = null;
        try {
            R r = wareFeignService.isHasStock(skuIds);
            if (r.getCode() == 0) {
                List<WareHasStockDTO> hasStockList = (List<WareHasStockDTO>) r.get("hasStockList");
                ObjectMapper objectMapper = new ObjectMapper();
                String str = objectMapper.writeValueAsString(hasStockList);
                JavaType javaType
                        = objectMapper.getTypeFactory().constructParametricType(List.class, WareHasStockDTO.class);
                hasStockList = objectMapper.readValue(str, javaType);
                hasStockMap = hasStockList.stream()
                        .collect(Collectors.toMap(item -> item.getSkuId(), item -> item.getHasStock()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hasStockMap;
    }


    // 对应的规格参数  根据spuId来查询规格参数信息
    @Override
    public List<SkuESModel.Attrs> getAttrsModel(Long spuId) {

        List<ProductAttrValueEntity> attrValueEntityList
                = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        List<Long> attrIdList = attrValueEntityList.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrService.listByIds(attrIdList);
        List<Long> retuenAttrIdList = attrEntities.stream().filter(item -> item.getSearchType() == 1).map(item -> item.getAttrId())
                .collect(Collectors.toList());

        List<SkuESModel.Attrs> attrs = attrValueEntityList.stream()
                .filter(item -> retuenAttrIdList.contains(item.getAttrId()))
                .map(item -> {
                    SkuESModel.Attrs attr = new SkuESModel.Attrs();
                    BeanUtils.copyProperties(item, attr);
                    return attr;
                }).collect(Collectors.toList());
        return attrs;
    }

    /**
     * spu检索
     * 分类
     * 品牌
     * 状态
     * key id,spu_name,spu_description
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isNullOrEmpty(key)) {
            wrapper.and(k -> {
                k.eq("id", key).or()
                        .like("spu_name", key).or()
                        .like("spu_description", key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isNullOrEmpty(status)) {
            wrapper.and(k -> k.eq("publish_status", status));
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isNullOrEmpty(brandId)) {
            wrapper.and(k -> k.eq("brand_id", brandId));
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isNullOrEmpty(catelogId)) {
            wrapper.and(k -> k.eq("catalog_id", catelogId));
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