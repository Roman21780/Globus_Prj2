package com.example.prj2.kafka;

import com.example.prj2.dto.ExchangeRateEvent;
import com.example.prj2.entity.ExchangeRate;
import com.example.prj2.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateConsumer {

    private final ExchangeRateRepository exchangeRateRepository;

    @KafkaListener(topics = "exchange-rates", groupId = "exchange-rate-group")
    public void consumeExchangeRateEvent(ExchangeRateEvent event) {
        try {
            log.info("Received event from Kafka: code={}, rate={}, type={}",
                    event.getCode(), event.getRate(), event.getEventType());

            ExchangeRate exchangeRate = exchangeRateRepository
                    .findByCode(event.getCode())
                    .orElse(ExchangeRate.builder()
                            .code(event.getCode())
                            .build());

            exchangeRate.setRate(event.getRate());
            exchangeRateRepository.save(exchangeRate);

            log.info("Exchange rate saved: code={}, rate={}",
                    event.getCode(), event.getRate());
        } catch (Exception e) {
            log.error("Failed to process exchange rate event", e);
        }
    }
}
