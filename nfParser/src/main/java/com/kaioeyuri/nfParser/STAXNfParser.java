package com.kaioeyuri.nfParser;

import com.kaioeyuri.entities.BrazilianInvoice;
import com.kaioeyuri.entities.Issuer;
import com.kaioeyuri.entities.Product;
import com.kaioeyuri.enums.CommercialUnit;
import com.kaioeyuri.enums.NationalRegisterType;
import com.kaioeyuri.nfParser.exceptions.MalformedXMLException;
import com.kaioeyuri.valueObjects.NationalRegister;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent; import java.io.Reader;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class STAXNfParser implements INfParser {
    @Override
    public BrazilianInvoice parse(Reader nfXMLReader) throws MalformedXMLException {
        XMLEventReader reader;
        try {
            reader = XMLInputFactory.newInstance().createXMLEventReader(nfXMLReader);
        } catch (XMLStreamException exception) {
            throw new MalformedXMLException(exception.getMessage());
        }

        BrazilianInvoice invoice = new BrazilianInvoice();

        try {
            this.readXMLTags(reader, invoice);
        } catch (XMLStreamException exception) {
            throw new MalformedXMLException("Failed to parse NF XML", exception);
        }

        return invoice;
    }

    private void readXMLTags(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                String element = event.asStartElement().getName().getLocalPart();

                switch (element) {
                    case NfeXMLTags.identitySectionTagName -> this.handleIdentitySection(reader, invoice);
                    case NfeXMLTags.buyerSectionTagName -> this.handleBuyerSection(reader, invoice);
                    case NfeXMLTags.issuerSectionTagName -> this.handleIssuerSection(reader, invoice);
                    case NfeXMLTags.totalSectionTagName -> this.extractTotalCost(reader, invoice);
                    case NfeXMLTags.productTag -> this.handleProductsSection(reader, invoice);
                }
            }

            if (event.isEndElement()) {
                String element = event.asEndElement().getName().getLocalPart();
                if (element.equals(NfeXMLTags.endOfRelevantDataTag)) break;
            }
        }
    }

    private void handleIdentitySection(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        List<String> targetElements = new ArrayList<>(List.of("cNF", "nNF", "dhEmi"));

        while (!targetElements.isEmpty() && reader.hasNext()) {
            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String elementName = element.getName().getLocalPart();

                if (targetElements.contains(elementName)) {
                    String value = reader.nextEvent().asCharacters().getData();
                    switch (elementName) {
                        case "cNF" -> invoice.setAccessKey(value);
                        case "nNF" -> invoice.setNfNumber(value);
                        case "dhEmi" -> {
                            Date date = Date.from(OffsetDateTime.parse(value).toInstant());
                            invoice.setIssuedAt(date);
                        }
                    }

                    targetElements.remove(elementName);
                }
            }

            if (event.isEndElement()) {
                String element = event.asEndElement().getName().getLocalPart();
                if (element.equals(NfeXMLTags.identitySectionTagName)) break;
            }
        }
    }

    public void handleIssuerSection(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();

        Issuer issuer = new Issuer();

        while (reader.hasNext() && (issuer.getNationalRegister() == null || issuer.getTradingName() == null)) {
            if (event.isStartElement()) {
                String element = event.asStartElement().getName().getLocalPart();

                if (element.equals("xFant")) {
                    String value = reader.nextEvent().asCharacters().getData();
                    issuer.setTradingName(value);
                    continue;
                }

                if (element.equals("CNPJ") || element.equals("CPF")) {
                    String value = reader.nextEvent().asCharacters().getData();
                    NationalRegister register = new NationalRegister(value, NationalRegisterType.valueOf(element));
                    issuer.setNationalRegister(register);
                }
            }

            if (event.isEndElement()) {
                String element = event.asEndElement().getName().getLocalPart();
                if (element.equals(NfeXMLTags.issuerSectionTagName)) break;
            }
        }

        invoice.setIssuer(issuer);
    }

    public void handleBuyerSection(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                String element = event.asStartElement().getName().getLocalPart();

                if (element.equals("CNPJ") || element.equals("CPF")) {
                    String value = reader.nextEvent().asCharacters().getData();
                    invoice.setBuyerRegister(new NationalRegister(value, NationalRegisterType.valueOf(element)));
                    break;
                }
            }

            if (event.isEndElement()) {
                String element = event.asEndElement().getName().getLocalPart();
                if (element.equals(NfeXMLTags.buyerSectionTagName)) break;
            }
        }
    }

    public void extractTotalCost(XMLEventReader reader, BrazilianInvoice invoice) throws  XMLStreamException {

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(NfeXMLTags.totalICMSSectionTagName)) {
                while(reader.hasNext()) {
                    XMLEvent innerEvent = reader.nextEvent();
                    if (innerEvent.isStartElement() && innerEvent.asStartElement().getName().getLocalPart().equals("vNF")) {
                        String value = reader.nextEvent().asCharacters().getData();
                        BigDecimal cost = new BigDecimal(value);
                        invoice.setTotalCost(cost);
                        return;
                    }
                }
            }
        }
    }

    public void handleProductsSection(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {

        Product product = new Product();

        List<String> targetElements = new ArrayList<>(List.of(
                NfeXMLTags.productCodeTag,
                NfeXMLTags.productNameTag,
                NfeXMLTags.productCommercialQuantityTag,
                NfeXMLTags.productUnitaryCostTag,
                NfeXMLTags.productTributaryUnitaryCostTag,
                NfeXMLTags.productCommercialUnityTag
        ));

        while(reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(NfeXMLTags.productInnerTag)) {
                while(reader.hasNext()) {
                    event = reader.nextEvent();

                    if (!event.isStartElement()) continue;

                    String element = event.asStartElement().getName().getLocalPart().toString();
                    if (!targetElements.contains(element)) continue;

                    String value = reader.nextEvent().asCharacters().getData();

                    switch (element) {
                        case NfeXMLTags.productCodeTag -> product.setCode(value);
                        case NfeXMLTags.productNameTag -> product.setName(value);
                        case NfeXMLTags.productCommercialQuantityTag -> product.setCommercialQuantity(new BigDecimal(value));
                        case NfeXMLTags.productUnitaryCostTag -> product.setUnitaryCost(new BigDecimal(value));
                        case NfeXMLTags.productTributaryUnitaryCostTag -> product.setTributaryUnitaryCost(new BigDecimal(value));
                        case NfeXMLTags.productCommercialUnityTag -> {
                            CommercialUnit commercialUnit;

                            try {
                                commercialUnit = CommercialUnit.valueOf(value);
                            } catch (IllegalArgumentException exception) {
                                throw new MalformedXMLException("Received invalid CommercialUnit variant", exception);
                            }

                            product.setCommercialUnity(commercialUnit);
                        }
                    }
                }

                continue;
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(NfeXMLTags.productTag)) break;
        }

        invoice.getMaterials().add(product);
    }
}
