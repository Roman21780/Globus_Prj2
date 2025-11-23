package com.example.prj2.service;

import com.example.prj2.dto.ExchangeRateEvent;
import com.example.prj2.entity.ExchangeRate;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final RestTemplate restTemplate;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateProducer exchangeRateProducer;

    private static final String CBR_URL = "https://cbr.ru/scripts/XML_daily.asp";
    private Map<String, String> rates = new HashMap<>();

    public void updateRatesFromCBR() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String xml = restTemplate.getForObject(CBR_URL + "?date_req=" + today, String.class);

            if (xml == null) {
                log.error("CBR responded with null XML");
                return;
            }

            Map<String, BigDecimal> parsedRates = parseXMLToBigDecimal(xml);
            rates = new HashMap<>();

            parsedRates.forEach((code, rate) -> {
                rates.put(code, rate.toString());

                // Сохраняем напрямую в БД
                saveRateToDB(code, rate);

                // Отправляем событие в Kafka для асинхронной обработки
                ExchangeRateEvent event = ExchangeRateEvent.builder()
                        .code(code)
                        .rate(rate)
                        .timestamp(LocalDateTime.now())
                        .eventType("UPDATE")
                        .build();

                exchangeRateProducer.sendExchangeRateEvent(event);
                log.info("Saved and sent to Kafka: {} = {}", code, rate);
            });

            log.info("Updated {} exchange rates from CBR and sent to Kafka", rates.size());
        } catch (Exception e) {
            log.error("Failed to update rates", e);
        }
    }

    // Сохранить или обновить курс в БД
    private void saveRateToDB(String code, BigDecimal rate) {
        try {
            Optional<ExchangeRate> existing = exchangeRateRepository.findByCode(code);

            if (existing.isPresent()) {
                // Обновить существующий курс
                ExchangeRate exchangeRate = existing.get();
                exchangeRate.setRate(rate);
                exchangeRateRepository.save(exchangeRate);
                log.debug("Updated rate in DB: {} = {}", code, rate);
            } else {
                // Создать новый курс
                ExchangeRate newRate = ExchangeRate.builder()
                        .code(code)
                        .rate(rate)
                        .build();
                exchangeRateRepository.save(newRate);
                log.debug("Created new rate in DB: {} = {}", code, rate);
            }
        } catch (Exception e) {
            log.error("Failed to save rate to DB: {}", code, e);
        }
    }

    public void fakeUpdateRates() {
        log.info("Fake update - no real changes (completed at {})", LocalDate.now());
    }

    public Map<String, String> getRates() {
        return new HashMap<>(rates);
    }

    public Map<String, BigDecimal> getAllRatesFromDB() {
        Map<String, BigDecimal> result = new HashMap<>();
        exchangeRateRepository.findAll().forEach(rate ->
                result.put(rate.getCode(), rate.getRate())
        );
        return result;
    }

    private Map<String, BigDecimal> parseXMLToBigDecimal(String xml) throws Exception {
        Map<String, BigDecimal> result = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

        NodeList valuteList = doc.getElementsByTagName("Valute");

        for (int i = 0; i < valuteList.getLength(); i++) {
            var valute = (org.w3c.dom.Element) valuteList.item(i);

            String code = valute.getElementsByTagName("CharCode").item(0).getTextContent();
            String rateStr = valute.getElementsByTagName("Value").item(0).getTextContent()
                    .replace(",", ".");
            BigDecimal rate = new BigDecimal(rateStr);

            result.put(code, rate);

            log.debug("Parsed: {} = {}", code, rate);
        }

        log.info("Successfully parsed {} exchange rates", result.size());
        return result;
    }
}
