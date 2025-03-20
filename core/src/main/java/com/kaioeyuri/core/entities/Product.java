package com.kaioeyuri.core.entities;

import com.kaioeyuri.core.enums.CommercialUnity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Product {
    private String code;
    private String name;
    private BigDecimal commercialQuantity;
    private CommercialUnity commercialUnity;
    private BigDecimal unitaryCost;
    private BigDecimal tributaryUnitaryCost;
}
