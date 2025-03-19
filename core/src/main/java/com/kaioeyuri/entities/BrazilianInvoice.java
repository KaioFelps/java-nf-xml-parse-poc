package com.kaioeyuri.entities;

import com.kaioeyuri.valueObjects.NationalRegister;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BrazilianInvoice {
    private String nfNumber;
    private String accessKey;
    private BigDecimal totalCost;
    private NationalRegister buyerRegister;
    private Date issuedAt;
    private Issuer issuer;
    private List<Product> materials;
}
