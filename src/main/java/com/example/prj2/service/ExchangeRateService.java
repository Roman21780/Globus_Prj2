package com.example.prj2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ExchangeRateService {

    private final RestTemplate restTemplate;
    private static final String CBR_URL = "https://cbr.ru/scripts/XML_daily.asp";
    private Map<String, String> rates = new HashMap<>();

    public ExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void updateRatesFromCBR() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String xml = restTemplate.getForObject(CBR_URL + "?date_req=" + today, String.class);

            if (xml == null) {
                log.error("CBR responded with null XML");
                return;
            }

            rates = parseXML(xml);
            log.info("Updated {} exchange rates from CBR", rates.size());
        } catch (Exception e) {
            log.error("Failed to update rates", e);
        }
    }

    public void fakeUpdateRates() {
        log.info("Fake update - no real changes (completed at {})", LocalDate.now());
    }

    public Map<String, String> getRates() {
        return new HashMap<>(rates);
    }

    private Map<String, String> parseXML(String xml) throws Exception {
        Map<String, String> result = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

        NodeList valuteList = doc.getElementsByTagName("Valute");

        for (int i = 0; i < valuteList.getLength(); i++) {
            var valute = valuteList.item(i);
            String code = valute.getChildNodes().item(2).getTextContent(); // CharCode
            String rate = valute.getChildNodes().item(5).getTextContent(); // Value
            result.put(code, rate);
        }

        return result;
    }
}
