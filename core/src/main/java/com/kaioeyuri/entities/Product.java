package com.kaioeyuri.entities;

import com.kaioeyuri.enums.CommercialUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
public class Product {
    private String code;
    private String name;
    private BigDecimal commercialQuantity;
    private CommercialUnit commercialUnity;
    private Currency unitaryCost;
    private Currency tributaryUnitaryCost;
}
