package com.msb.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀实体
 */
@Data
public class SeckillVo {

    private Long skuId;
    private BigDecimal seckillPrice;
    private BigDecimal seckillCount;
    private BigDecimal seckillLimit;
    private Integer seckillSort;
    private Long startTime;
    private Long endTime;
    private Long promotionId;
    private Long promotionSessionId;
    // 随机码
    private String randCode;

}
