package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.vo.Catalog2VO;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询所有的类别数据，然后将数据封装为树形结构，便于前端使用
     */
    public List<CategoryEntity> queryPageWithTree(Map<String, Object> params);

    /**
     * 逻辑批量删除操作
     * @param ids
     */
    public void removeCategoryByIds(List<Long> ids);

    Long[] findCatelogPath(Long catelogId);

    /**
     * 查询出所有的商品大类(一级分类)
     * @return
     */
    public List<CategoryEntity> getLeve1Category();

    Map<String, List<Catalog2VO>> getCatelog2JSON();
}

