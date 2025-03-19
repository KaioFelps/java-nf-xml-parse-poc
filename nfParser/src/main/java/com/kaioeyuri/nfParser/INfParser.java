package com.kaioeyuri.nfParser;

import com.kaioeyuri.entities.BrazilianInvoice;
import com.kaioeyuri.nfParser.exceptions.MalformedXMLException;

import java.io.Reader;

public interface INfParser{
    public BrazilianInvoice parse(Reader nfXMLReader) throws MalformedXMLException;
}
