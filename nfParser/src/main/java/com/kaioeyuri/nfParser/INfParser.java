package com.kaioeyuri.nfParser;

import com.kaioeyuri.entities.BrazilianInvoice;

public interface INfParser{
    public BrazilianInvoice parse(String nfXML);
}
