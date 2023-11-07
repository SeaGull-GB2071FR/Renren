package com.msb.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.jackson.JsonComponent;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonComponent
public class WareHasStockDTO {

    private Long skuId;

    private Boolean hasStock;

}
