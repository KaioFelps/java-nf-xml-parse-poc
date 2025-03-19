package com.kaioeyuri.valueObjects;

import com.kaioeyuri.enums.NationalRegisterType;

import java.util.Optional;

public class NationalRegister {
    private NationalRegisterType type;
    private String value;

    public NationalRegister(String value, NationalRegisterType type) {
        this.type = type;
        this.value = value;
    }

    public Optional<String> getCPF() {
        if (this.type.equals(NationalRegisterType.CPF)) {
            return Optional.of(this.value);
        }

        return Optional.empty();
    }

    public Optional<String> getCNPJ() {
        if (this.type.equals(NationalRegisterType.CNPJ)) {
            return Optional.of(this.value);
        }

        return Optional.empty();
    }
}
