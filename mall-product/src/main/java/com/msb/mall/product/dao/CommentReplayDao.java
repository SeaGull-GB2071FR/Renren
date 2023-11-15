package com.msb.mall.product.dao;

import com.msb.mall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
