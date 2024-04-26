package com.msb.mall.order.controller;

import java.util.Arrays;
import java.util.Map;


import com.msb.mall.order.feign.ProductFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.msb.mall.order.entity.OmsOrderEntity;
import com.msb.mall.order.service.OmsOrderService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;



/**
 * 订单
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-10-30 17:31:57
 */
@RefreshScope   // 动态的刷新配置数据
@RestController
@RequestMapping("order/omsorder")
public class OmsOrderController {
    @Autowired
    private OmsOrderService omsOrderService;

    @Autowired
    private ProductFeignService productFeignService;

    @GetMapping("/products")
    public R queryProduct(){
        // OpenFegin 远程调用服务
        return R.ok().put("products", productFeignService.queryAllBrand());
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:omsorder:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = omsOrderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:omsorder:info")
    public R info(@PathVariable("id") Long id){
		OmsOrderEntity omsOrder = omsOrderService.getById(id);

        return R.ok().put("omsOrder", omsOrder);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:omsorder:save")
    public R save(@RequestBody OmsOrderEntity omsOrder){
		omsOrderService.save(omsOrder);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:omsorder:update")
    public R update(@RequestBody OmsOrderEntity omsOrder){
		omsOrderService.updateById(omsOrder);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:omsorder:delete")
    public R delete(@RequestBody Long[] ids){
		omsOrderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
