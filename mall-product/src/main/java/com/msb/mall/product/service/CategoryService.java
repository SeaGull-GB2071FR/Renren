package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author dpb
 * @email dengpbs@163.com
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

}

