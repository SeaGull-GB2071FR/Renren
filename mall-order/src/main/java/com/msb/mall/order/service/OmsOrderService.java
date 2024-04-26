package com.msb.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.dto.SeckillOrderDto;
import com.msb.common.utils.PageUtils;
import com.msb.mall.order.dto.OrderCreateTO;
import com.msb.mall.order.entity.OmsOrderEntity;
import com.msb.mall.order.vo.OrderConfirmVo;
import com.msb.mall.order.vo.OrderResponseVO;
import com.msb.mall.order.vo.OrderSubmitVO;

import java.util.Map;

/**
 * 订单
 *
 * @author dpb
 * @email dengpbs@163.com
 * @date 2023-10-30 17:31:57
 */
public interface OmsOrderService extends IService<OmsOrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    OrderResponseVO submitOrder(OrderSubmitVO vo);

    OrderCreateTO createOrder(OrderSubmitVO vo);

}

