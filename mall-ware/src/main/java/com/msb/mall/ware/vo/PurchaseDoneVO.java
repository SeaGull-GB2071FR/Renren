package com.msb.mall.ware.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 采购单的VO数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDoneVO {

    private Long id;

    private List<PurchaseItemDoneVO> items;

}