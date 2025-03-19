package com.kaioeyuri.nfParser;

import com.kaioeyuri.entities.BrazilianInvoice;

public class STAXNfParser implements INfParser{
    @Override
    public BrazilianInvoice parse(String nfXML) {
        return new BrazilianInvoice();
    }
}
