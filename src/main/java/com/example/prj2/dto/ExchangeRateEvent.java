package com.example.prj2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateEvent {

    @JsonProperty("code")
    private String code;

    @JsonProperty("rate")
    private BigDecimal rate;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("eventType")
    private String eventType;
}
