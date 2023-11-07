package com.msb.mall.product.vo;


import lombok.Data;

@Data
public class AttrVo {

    private Long attrId;

    private String attrName;

    private Integer searchType;

    private String icon;

    private String valueSelect;

    private Integer attrType;

    private Long enable;

    private Long catelogId;

    private Integer showDesc;

    /**
     * 记录规格参数对应的属性组
     */
    private Long attrGroupId;
}
