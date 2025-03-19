package com.kaioeyuri.core.entities;

import com.kaioeyuri.core.enums.CommercialUnit;
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
    private CommercialUnit commercialUnity;
    private BigDecimal unitaryCost;
    private BigDecimal tributaryUnitaryCost;
}
