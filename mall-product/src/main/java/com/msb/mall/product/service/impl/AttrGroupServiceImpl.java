package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.msb.mall.product.dao.AttrAttrgroupRelationDao;
import com.msb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.AttrService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.AttrGroupWithAttrsVo;
import com.msb.mall.product.vo.SpuItemGroupAttrVo;
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

import com.msb.mall.product.dao.AttrGroupDao;
import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询列表数据
     * 根据列表编号来查询
     *
     * @param params
     * @param cateLogId
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long cateLogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            // 拼接查询的条件
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (cateLogId == 0) {
            // 不根据catelogId来查询
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params), wrapper
            );
            return new PageUtils(page);
        }
        // 根据类别编号来查询属性信息
        wrapper.eq("catelog_id", cateLogId);
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params), wrapper
        );
        return new PageUtils(page);
    }

    /**
     * 1.根据三级分类的编号查询出对应的所有的属性组
     * 2.根据属性组查询到对应的属性信息
     *
     * @return
     */

    @Override
    public List<AttrGroupWithAttrsVo> getAttrgroupWithAttrsByCatelogId(Long catelogId) {
        //1.根据三级分类的编号查询出对应的所有的属性组

        List<AttrGroupEntity> attrGroups = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> list = attrGroups.stream().map((group) -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, attrGroupWithAttrsVo);
            // 根据属性组找到所有的属性
            List<AttrEntity> attrEntities = attrService.getRelationAttr(group.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrEntities);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());

        return list;
    }

    @Override
    public List<SpuItemGroupAttrVo> getAttrgroupWithSpuId(Long spuId, Long catalogId) {
        //
        List<SpuItemGroupAttrVo> groupAttrVo = attrGroupDao.getAttrgroupWithSpuId(spuId,catalogId);
        return groupAttrVo;
    }


}