/**
 * Copyright 2022 json.cn
 */
package com.msb.mall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author GB2071FR
 * @email 1184800897@qq.com
 * @date 2023-10-30 17:10:05
 */
@Data
public class SpuInfoVo {
    private Long id;

    private String spuName;

    private String spuDescription;

    private Long catalogId;

    private String catalogName;

    private Long brandId;

    private String brandName;

    private BigDecimal weight;

    private Integer publishStatus;

    private List<String> decript;

    private List<String> images;

    private Bounds bounds;

    private List<BaseAttrs> baseAttrs;

    private List<Skus> skus;

    private Date createTime;

    private Date updateTime;



}