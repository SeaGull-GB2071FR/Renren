package com.msb.mall.product.service.impl;

import com.msb.mall.product.entity.*;
import com.msb.mall.product.service.*;
import com.msb.mall.product.vo.SpuInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private SkuInfoService skuInfoService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuInfoVo spuInfoVo) {
        // 1.保存spu的基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVo, spuInfoEntity);

        // 创建时间和更新时间需要手动来同步
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);

        // 2.保存spu的详情信息 pms_spu_info_desc
        List<String> decript = spuInfoVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.save(spuInfoDescEntity);

        // 3.保存图集信息 pms_spu_images
        List<String> images = spuInfoVo.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map((item) -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfoEntity.getId());
            spuImagesEntity.setImgUrl(item);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntities);

        // 4.保存规格参数 pms_product_attr_value


        // 5.保存当前的spu对应的所有的sku信息
        // 5.1 保存sku的基本信息 pms_sku_info
        // 5.2 保存sku的图片信息 pms_sku_image
        // 5.3 保存满减信息，折扣，会员价 mall_sms: sms_sku_ladder sms_full_reduction sms_member_price

        // 5.4 sku的销售属性信息 pms_sku_sale_attr_value


        // 6.保存spu的积分信息: mall_sms: sms_spu_bounds

    }


}