package com.msb.mall.order.dto;

import com.msb.mall.order.entity.OmsOrderEntity;
import com.msb.mall.order.entity.OmsOrderItemEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateTO {

    private OmsOrderEntity orderEntity; // 订单信息

    private List<OmsOrderItemEntity> orderItemEntitys; // 订单信息
}
