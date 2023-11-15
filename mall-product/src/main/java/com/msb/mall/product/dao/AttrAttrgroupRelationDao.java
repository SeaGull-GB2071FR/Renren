package com.msb.mall.product.dao;

import com.msb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性&属性分组关联
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void removeBatchRelation(List<AttrAttrgroupRelationEntity> list);
}
