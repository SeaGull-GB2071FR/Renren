package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.vo.AttrGroupRelationVo;
import com.msb.mall.product.vo.AttrResponseVo;
import com.msb.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoAttrRelation(Map<String, Object> params, Long attrgroupId);

    void saveAttr(AttrVo vo);

    PageUtils queryBasePage(Map<String, Object> params, Long catelogId);

    AttrResponseVo getAttrInfo(Long attrId);

    void updateBaseAttr(AttrVo attr);

    PageUtils queryPageDetail(Map<String, Object> params, String type, Long cateId);
}

