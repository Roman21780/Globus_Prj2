package com.example.prj2.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void testFakeUpdateRates() {
        assertDoesNotThrow(() -> exchangeRateService.fakeUpdateRates());
    }

    @Test
    void testGetRates() {
        Map<String, String> rates = exchangeRateService.getRates();
        assertNotNull(rates);
    }

    @Test
    void testUpdateRatesFromCBR() {
        String mockXml = "<?xml version=\"1.0\" encoding=\"Windows-1251\"?>" +
                "<ValCurs>" +
                "<Valute ID=\"R01235\">" +
                "<CharCode>USD</CharCode>" +
                "<Value>100,50</Value>" +
                "</Valute>" +
                "</ValCurs>";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockXml);

        exchangeRateService.updateRatesFromCBR();
        Map<String, String> rates = exchangeRateService.getRates();

        assertTrue(rates.containsKey("USD"));
    }
}
