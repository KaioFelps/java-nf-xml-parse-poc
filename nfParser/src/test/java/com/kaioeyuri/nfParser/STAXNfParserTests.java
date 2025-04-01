package com.kaioeyuri.nfParser;

import static org.junit.jupiter.api.Assertions.*;

import com.kaioeyuri.core.entities.BrazilianInvoice;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class STAXNfParserTests {
    @Test
    void shouldReadAndParseAValidNFeXML() throws Exception {
        Reader reader = new FileReader("src/test/fixtures/nfe-template.xml");
        BrazilianInvoice invoice = new STAXNfParser().parse(reader);

        assertNotNull(invoice, "Invoice object shouldn't be null.");
        assertEquals(2, invoice.getMaterials().size());

        assertEquals(1, invoice.getNfNumber(), "It should be able to get the NF number.");
        assertEquals("518005127", invoice.getAccessKey(), "It should be able to get the NF access key.");

        assertTrue(invoice.getBuyerRegister().getCNPJ().isPresent());
        assertEquals(
                "00000000000191",
                invoice.getBuyerRegister().getCNPJ().get(),
                "It should be able to get the recipient/receiver national register (CNPJ or CPF)."
        );
        assertEquals(
                new BigDecimal("20000000.00"),
                invoice.getTotalCost(),
                "It should be able to get the NF total value."
        );
        assertEquals(
                "2008-05-06T00:00:00-03:00",
                DateTimeFormatter
                        .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                        .format(invoice
                                .getIssuedAt()
                                .toInstant()
                                .atZone(ZoneId.of("America/Sao_Paulo"))),
                "It should be able to get the date and time of the NF emission."
        );

        assertTrue(invoice.getIssuer().getNationalRegister().getCNPJ().isPresent());
        assertEquals(
                "99999090910270",
                invoice.getIssuer().getNationalRegister().getCNPJ().get(),
                "It should be able to get the issuer national register (CNPJ or CPF)."
        );
        assertEquals(
                "NF-e",
                invoice.getIssuer().getTradingName(),
                "It should be able to get the issuer trading name."
        );
    }
}
