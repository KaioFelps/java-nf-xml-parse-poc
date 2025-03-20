package com.kaioeyuri.core.enums;

import java.util.HashMap;
import java.util.Map;

public enum CommercialUnity {
    AMPOLA("AMPOLA"),
    BALDE("BALDE"),
    BANDEJ("BANDEJA"),
    BARRA("BARRA"),
    BISNAG("BISNAGA"),
    BLOCO("BLOCO"),
    BOBINA("BOBINA"),
    BOMBEAR("BOMBONA"),
    CÁPSULAS("CÁPSULA"),
    CART("CARTELA"),
    CENTO("CENTO"),
    CJ("CONJUNTO"),
    CM("CENTÍMETRO"),
    CM2("CENTIMETRO QUADRADO"),
    CX("CAIXA"),
    CX2("CAIXA COM 2 UNIDADES"),
    CX3("CAIXA COM 3 UNIDADES"),
    CX5("CAIXA COM 5 UNIDADES"),
    CX10("CAIXA COM 10 UNIDADES"),
    CX15("CAIXA COM 15 UNIDADES"),
    CX20("CAIXA COM 20 UNIDADES"),
    CX25("CAIXA COM 25 UNIDADES"),
    CX50("CAIXA COM 50 UNIDADES"),
    CX100("CAIXA COM 100 UNIDADES"),
    DISP("DISPLAY"),
    DUZIA("DUZIA"),
    EMBAL("EMBALAGEM"),
    FARDO("FARDO"),
    FOLHA("FOLHA"),
    FRASCO("FRASCO"),
    GALAO("GALÃO"),
    GF("GARRAFA"),
    GRAMAS("GRAMAS"),
    JOGO("JOGO"),
    KG("QUILOGRAMA"),
    KIT("KIT"),
    LATA("LATA"),
    LITRO("LITRO"),
    M("METRO"),
    M2("METRO QUADRADO"),
    M3("METRO CÚBICO"),
    MILHEI("MILHEIRO"),
    ML("MILILITRO"),
    MWH("MEGAWATT HORA"),
    PACOTE("PACOTE"),
    PALETE("PALETE"),
    PARES("PARES"),
    PC("PEÇA"),
    AMIGO("AMIGO"),
    K("QUILATE"),
    RESMA("RESMA"),
    ROLO("ROLO"),
    SACO("SACO"),
    SACOLA("SACOLA"),
    TAMBOR("TAMBOR"),
    TANQUE("TANQUE"),
    TON("TONELADA"),
    TUBO("TUBO"),
    UNID("UNIDADE"),
    VASIL("VASILHAME"),
    VIDRO("VIDRO"),
    GS("GROSA"),
    PA("PAR");

    public final String label;

    public static final Map<String, CommercialUnity> ALIASES = new HashMap<>();

    static {
        for (CommercialUnity unity : values()) {
            ALIASES.put(unity.toString(), unity);
        }
        // add custom aliases
        ALIASES.put("LT", CommercialUnity.LITRO);
        ALIASES.put("PR", CommercialUnity.PA);
        ALIASES.put("RL", CommercialUnity.ROLO);
        ALIASES.put("UN", CommercialUnity.UNID);
        ALIASES.put("CT", CommercialUnity.CART);
        ALIASES.put("DZ", CommercialUnity.DUZIA);
        ALIASES.put("PT", CommercialUnity.PACOTE);
        ALIASES.put("PACK", CommercialUnity.PACOTE);
    };

    public static CommercialUnity fromString(String value) {
        CommercialUnity unity = CommercialUnity.ALIASES.get(value.toUpperCase());

        if (unity == null) {
            throw new IllegalArgumentException("Received invalid Commercial Unity: " + value);
        }

        return unity;
    }

    private CommercialUnity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
