package com.kaioeyuri.core.entities;

import com.kaioeyuri.core.valueObjects.NationalRegister;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BrazilianInvoice {
    private Integer nfNumber;
    private String accessKey;
    private BigDecimal totalCost;
    private NationalRegister buyerRegister;
    private Date issuedAt;
    private Issuer issuer;
    private List<Product> materials;
}
