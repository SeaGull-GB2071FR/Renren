package com.msb.mall.ware.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 采购项的VO数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemDoneVO {
    private Long itemId;
    private Integer status;
    private String reason;
}
