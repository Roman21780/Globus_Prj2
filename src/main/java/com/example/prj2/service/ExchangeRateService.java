package com.example.prj2.service;

import com.example.prj2.dto.ExchangeRateEvent;
import com.example.prj2.kafka.ExchangeRateProducer;
import com.example.prj2.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final RestTemplate restTemplate;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateProducer exchangeRateProducer;

    private static final String CBR_URL = "https://cbr.ru/scripts/XML_daily.asp";
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private Map<String, String> rates = new HashMap<>();

    /**
     * Обновить курсы валют из ЦБР и отправить события в Kafka
     * Consumer будет слушать топик и сохранять данные в БД
     */
    public void updateRatesFromCBR() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN));
            String url = CBR_URL + "?date_req=" + today;

            log.info("Fetching exchange rates from CBR: {}", url);
            String xml = restTemplate.getForObject(url, String.class);

            if (xml == null || xml.isEmpty()) {
                log.warn("CBR responded with empty XML");
                return;
            }

            Map<String, BigDecimal> parsedRates = parseXMLToBigDecimal(xml);

            if (parsedRates.isEmpty()) {
                log.warn("No exchange rates were parsed from XML");
                return;
            }

            // Отправляем каждый курс в Kafka как событие
            parsedRates.forEach((code, rate) -> {
                try {
                    ExchangeRateEvent event = ExchangeRateEvent.builder()
                            .code(code)
                            .rate(rate)
                            .timestamp(LocalDateTime.now())
                            .eventType("UPDATE")
                            .build();

                    exchangeRateProducer.sendExchangeRateEvent(event);
                    log.debug("Sent event to Kafka: code={}, rate={}", code, rate);
                } catch (Exception e) {
                    log.error("Failed to send event for code: {}", code, e);
                }
            });

            log.info("Successfully sent {} exchange rate events to Kafka", parsedRates.size());
        } catch (Exception e) {
            log.error("Failed to update rates from CBR", e);
        }
    }

    // Фейковое обновление (для тестирования)
    public void fakeUpdateRates() {
        log.info("Fake update executed at {}", LocalDate.now());
    }

    // Получить курсы из кэша (памяти)
    public Map<String, String> getRates() {
        return new HashMap<>(rates);
    }

    // Получить все курсы из базы данных
    public Map<String, BigDecimal> getAllRatesFromDB() {
        try {
            Map<String, BigDecimal> result = new HashMap<>();
            exchangeRateRepository.findAll().forEach(rate ->
                    result.put(rate.getCode(), rate.getRate())
            );
            log.debug("Retrieved {} rates from database", result.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to get rates from database", e);
            return new HashMap<>();
        }
    }

    // Парсинг XML из ЦБР и извлечь курсы валют
    private Map<String, BigDecimal> parseXMLToBigDecimal(String xml) throws Exception {
        Map<String, BigDecimal> result = new HashMap<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Отключить внешние DTD для безопасности
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

            NodeList valuteList = doc.getElementsByTagName("Valute");
            log.debug("Found {} Valute elements in XML", valuteList.getLength());

            for (int i = 0; i < valuteList.getLength(); i++) {
                try {
                    var valute = (org.w3c.dom.Element) valuteList.item(i);

                    String code = valute.getElementsByTagName("CharCode").item(0).getTextContent().trim();
                    String rateStr = valute.getElementsByTagName("Value").item(0).getTextContent()
                            .trim()
                            .replace(",", ".");

                    BigDecimal rate = new BigDecimal(rateStr);

                    result.put(code, rate);
                    log.debug("Parsed: {} = {}", code, rate);
                } catch (Exception e) {
                    log.warn("Failed to parse Valute element at index {}", i, e);
                    // Продолжаем с остальными элементами
                }
            }

            log.info("Successfully parsed {} exchange rates from XML", result.size());
        } catch (Exception e) {
            log.error("Failed to parse XML", e);
            throw e;
        }

        return result;
    }
}
