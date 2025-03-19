package com.kaioeyuri.entities;

import com.kaioeyuri.valueObjects.NationalRegister;
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
