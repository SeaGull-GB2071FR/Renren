package com.msb.mall.product.dao;

import com.msb.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-10-30 17:10:05
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
