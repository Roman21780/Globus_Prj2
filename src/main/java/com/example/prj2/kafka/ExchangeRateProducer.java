package com.example.prj2.kafka;

import com.example.prj2.dto.ExchangeRateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateProducer {

    private final KafkaTemplate<String, ExchangeRateEvent> kafkaTemplate;

    public void sendExchangeRateEvent(ExchangeRateEvent event) {
        try {
            Message<ExchangeRateEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, "exchange-rates")
                    .setHeader("kafka_messageKey", event.getCode())
                    .setHeader("timestamp", LocalDateTime.now().toString())
                    .build();

            kafkaTemplate.send(message);
            log.info("Event sent to Kafka: code={}, rate={}, type={}",
                    event.getCode(), event.getRate(), event.getEventType());
        } catch (Exception e) {
            log.error("Failed to send event to Kafka", e);
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
