package com.kaioeyuri.nfParser;

import com.kaioeyuri.core.entities.BrazilianInvoice;
import com.kaioeyuri.core.entities.Issuer;
import com.kaioeyuri.core.entities.Product;
import com.kaioeyuri.core.enums.CommercialUnity;
import com.kaioeyuri.core.enums.NationalRegisterType;
import com.kaioeyuri.nfParser.exceptions.MalformedXMLException;
import com.kaioeyuri.core.valueObjects.NationalRegister;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent; import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
                if (element.equalsIgnoreCase(NfeXMLTags.endOfRelevantDataTag)) break;
            }
        }
    }

    private void handleIdentitySection(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {
        final String[] dateTimeTagsAliases = {"dhEmi", "dEmi"};
        List<String> targetElements = new ArrayList<>(List.of("cNF", "nNF"));
        targetElements.addAll(List.of(dateTimeTagsAliases));

        while (!targetElements.isEmpty() && reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                StartElement element = event.asStartElement();
                String elementName = element.getName().getLocalPart();

                if (!targetElements.contains(elementName)) continue;

                event = reader.nextEvent();
                String value = event.asCharacters().getData();
                switch (elementName) {
                    case "cNF" -> {
                        invoice.setAccessKey(value);
                        targetElements.remove(elementName);
                    }
                    case "nNF" -> {
                        invoice.setNfNumber(Integer.parseInt(value));
                        targetElements.remove(elementName);
                    }
                    case "dhEmi" -> {
                        Date date = Date.from(OffsetDateTime.parse(value).toInstant());
                        invoice.setIssuedAt(date);
                        Arrays.stream(dateTimeTagsAliases).forEach(targetElements::remove);
                    }
                    case "dEmi" -> {
                        Date date;

                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                        } catch (ParseException exception) {
                            throw new MalformedXMLException("Failed to serialize emission date from NF-e dEmi field.", exception);
                        }

                        invoice.setIssuedAt(date);
                        Arrays.stream(dateTimeTagsAliases).forEach(targetElements::remove);
                    }
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(NfeXMLTags.issuerSectionTagName))
                break;
        }
    }

    public void handleIssuerSection(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {
        Issuer issuer = new Issuer();

        while (reader.hasNext() && (issuer.getNationalRegister() == null || issuer.getTradingName() == null)) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                String element = event.asStartElement().getName().getLocalPart();

                if (element.equalsIgnoreCase(NfeXMLTags.issuerTradingNameTag)) {
                    event = reader.nextEvent();
                    String value = event.asCharacters().getData();
                    issuer.setTradingName(value);
                    continue;
                }

                if (element.equalsIgnoreCase(NfeXMLTags.cnpjTag) || element.equalsIgnoreCase(NfeXMLTags.cpfTag)) {
                    event = reader.nextEvent();
                    String value = event.asCharacters().getData();
                    NationalRegister register = new NationalRegister(value, NationalRegisterType.valueOf(element));
                    issuer.setNationalRegister(register);
                }
            }

            if (event.isEndElement()) {
                String element = event.asEndElement().getName().getLocalPart();
                if (element.equalsIgnoreCase(NfeXMLTags.issuerSectionTagName)) break;
            }
        }

        invoice.setIssuer(issuer);
    }

    public void handleBuyerSection(XMLEventReader reader, BrazilianInvoice invoice) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                String element = event.asStartElement().getName().getLocalPart();

                if (element.equalsIgnoreCase(NfeXMLTags.cnpjTag) || element.equalsIgnoreCase(NfeXMLTags.cpfTag)) {
                    String value = reader.nextEvent().asCharacters().getData();
                    invoice.setBuyerRegister(new NationalRegister(value, NationalRegisterType.valueOf(element)));
                    break;
                }
            }

            if (event.isEndElement()) {
                String element = event.asEndElement().getName().getLocalPart();
                if (element.equalsIgnoreCase(NfeXMLTags.buyerSectionTagName)) break;
            }
        }
    }

    public void extractTotalCost(XMLEventReader reader, BrazilianInvoice invoice) throws  XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equalsIgnoreCase(NfeXMLTags.totalICMSSectionTagName)) {
                while(reader.hasNext()) {
                    XMLEvent innerEvent = reader.nextEvent();
                    if (innerEvent.isStartElement() && innerEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase(NfeXMLTags.nfValueTag)) {
                        String value = reader.nextEvent().asCharacters().getData();
                        BigDecimal cost = new BigDecimal(value);
                        invoice.setTotalCost(cost);
                        return;
                    }
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(NfeXMLTags.totalICMSSectionTagName)) {
                break;
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

            if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equalsIgnoreCase(NfeXMLTags.productInnerTag)) {
                while(reader.hasNext()) {
                    event = reader.nextEvent();

                    if (event.isStartElement()) {
                        String element = event.asStartElement().getName().getLocalPart();
                        if (!targetElements.contains(element)) continue;

                        String value = reader.nextEvent().asCharacters().getData();

                        switch (element) {
                            case NfeXMLTags.productCodeTag -> product.setCode(value);
                            case NfeXMLTags.productNameTag -> product.setName(value);
                            case NfeXMLTags.productCommercialQuantityTag ->
                                    product.setCommercialQuantity(new BigDecimal(value));
                            case NfeXMLTags.productUnitaryCostTag -> product.setUnitaryCost(new BigDecimal(value));
                            case NfeXMLTags.productTributaryUnitaryCostTag ->
                                    product.setTributaryUnitaryCost(new BigDecimal(value));
                            case NfeXMLTags.productCommercialUnityTag -> {
                                CommercialUnity commercialUnity;

                                try {
                                    commercialUnity = CommercialUnity.fromString(value);
                                } catch (IllegalArgumentException exception) {
                                    throw new MalformedXMLException("Received invalid CommercialUnity variant '" + value + "'.", exception);
                                }

                                product.setCommercialUnity(commercialUnity);
                            }
                        }
                    }

                    if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(NfeXMLTags.productInnerTag)) {
                        break;
                    }
                }
            }

            if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equalsIgnoreCase(NfeXMLTags.productTag)) break;
        }

        if (invoice.getMaterials() == null) invoice.setMaterials(new ArrayList<>());
        invoice.getMaterials().add(product);
    }
}
