package com.msb.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderDto {

    private Long memberId;
    private String memberName;
    private String orderSN;
    private Long skuId;
    private BigDecimal seckillPrice;
    private int count;
    private Long promotionSessionId;

}
