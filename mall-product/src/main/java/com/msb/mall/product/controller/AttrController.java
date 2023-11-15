package com.msb.mall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.msb.mall.product.vo.AttrResponseVo;
import com.msb.mall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.service.AttrService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;


/**
 * 商品属性
 *
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     *
     */
    @GetMapping("/base/list/{catelogId}")
    public R baseList(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrService.queryBasePage(params, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
//        AttrEntity attr = attrService.getById(attrId);
        AttrResponseVo attr = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
//    public R save(@RequestBody AttrEntity attr){
    public R save(@RequestBody AttrVo vo) {
//		attrService.save(attr);
        attrService.saveAttr(vo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateBaseAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
