package com.kaioeyuri.core.entities;

import com.kaioeyuri.core.valueObjects.NationalRegister;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Issuer {
    private NationalRegister nationalRegister;
    private String tradingName;
}
