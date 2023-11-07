package com.msb.common.dto.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品上架--需要保存到ElasticSearch中的数据
 */
@Data
public class SkuESModel {
    private Long skuId;
    private Long spuId;
    /**
     * 标题
     */
    private String subTitle;
    /**
     * 价格
     */
    private BigDecimal skuPrice;
    private String skuImg;
    /**
     * 销量
     */
    private Long saleCount;
    /**
     * 是否有库存
     */
    private Boolean hasStock;
    /**
     * 热度
     */
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
