package com.example.prj2.controller;

import com.example.prj2.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<Map<String, String>> getRates() {
        return ResponseEntity.ok(exchangeRateService.getRates());
    }

    @GetMapping("/db")
    public  ResponseEntity<Map<String, BigDecimal>> getRatesFromDB() {
        return ResponseEntity.ok(exchangeRateService.getAllRatesFromDB());
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateRates() {
        exchangeRateService.updateRatesFromCBR();
        return ResponseEntity.ok("Updated and sent to Kafka");
    }

    @PostMapping("/fake-update")
    public ResponseEntity<String> fakeUpdate() {
        exchangeRateService.fakeUpdateRates();
        return ResponseEntity.ok("Fake updated");
    }
}
