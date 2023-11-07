package com.msb.common.dto;

import lombok.Data;

@Data
public class OrderSpuDTO {

    private Long skuId;

    private String skuName;

    private Long spuId;
    private String spuName;
    // 图片
    private String spuPic;
    // 品牌
    private String spuBrand;
    // 分类id
    private Long categoryId;

}
