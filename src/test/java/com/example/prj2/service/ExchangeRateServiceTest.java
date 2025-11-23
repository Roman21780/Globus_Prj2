package com.example.prj2.service;

import com.example.prj2.dto.ExchangeRateEvent;
import com.example.prj2.entity.ExchangeRate;
import com.example.prj2.kafka.ExchangeRateProducer;
import com.example.prj2.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateService Tests")
class ExchangeRateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateProducer exchangeRateProducer;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private String mockXml;
    private String mockXmlMultiple;
    private String invalidXml;

    @BeforeEach
    void setUp() {
        // Минимальный валидный XML с одной валютой
        mockXml = "<?xml version=\"1.0\" encoding=\"Windows-1251\"?>" +
                "<ValCurs>" +
                "<Valute ID=\"R01235\">" +
                "<CharCode>USD</CharCode>" +
                "<Value>100,50</Value>" +
                "</Valute>" +
                "</ValCurs>";

        // XML с несколькими валютами
        mockXmlMultiple = "<?xml version=\"1.0\" encoding=\"Windows-1251\"?>" +
                "<ValCurs>" +
                "<Valute ID=\"R01235\">" +
                "<CharCode>USD</CharCode>" +
                "<Value>100,50</Value>" +
                "</Valute>" +
                "<Valute ID=\"R01239\">" +
                "<CharCode>EUR</CharCode>" +
                "<Value>110,25</Value>" +
                "</Valute>" +
                "<Valute ID=\"R01375\">" +
                "<CharCode>CNY</CharCode>" +
                "<Value>14,85</Value>" +
                "</Valute>" +
                "</ValCurs>";

        // Невалидный XML
        invalidXml = "<invalid></xml>";
    }

    @Test
    @DisplayName("Should update rates from CBR and send events to Kafka")
    void testUpdateRatesFromCBR_Success() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockXmlMultiple);

        doNothing().when(exchangeRateProducer)
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        ArgumentCaptor<ExchangeRateEvent> captor = ArgumentCaptor.forClass(ExchangeRateEvent.class);
        verify(exchangeRateProducer, times(3))
                .sendExchangeRateEvent(captor.capture());

        List<ExchangeRateEvent> events = captor.getAllValues();
        assertThat(events).hasSize(3);
        assertThat(events).extracting(ExchangeRateEvent::getCode)
                .containsExactlyInAnyOrder("USD", "EUR", "CNY");
        assertThat(events).extracting(ExchangeRateEvent::getEventType)
                .containsOnly("UPDATE");
    }

    @Test
    @DisplayName("Should parse exchange rates correctly with comma as decimal separator")
    void testUpdateRatesFromCBR_CorrectParsing() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockXml);

        doNothing().when(exchangeRateProducer)
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        ArgumentCaptor<ExchangeRateEvent> captor = ArgumentCaptor.forClass(ExchangeRateEvent.class);
        verify(exchangeRateProducer, times(1))
                .sendExchangeRateEvent(captor.capture());

        ExchangeRateEvent event = captor.getValue();
        assertThat(event.getCode()).isEqualTo("USD");
        assertThat(event.getRate()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(event.getEventType()).isEqualTo("UPDATE");
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle null XML response from CBR")
    void testUpdateRatesFromCBR_NullResponse() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(null);

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        verify(exchangeRateProducer, never())
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));
    }

    @Test
    @DisplayName("Should handle empty XML response from CBR")
    void testUpdateRatesFromCBR_EmptyResponse() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("");

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        verify(exchangeRateProducer, never())
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));
    }

    @Test
    @DisplayName("Should handle invalid XML without throwing exception")
    void testUpdateRatesFromCBR_InvalidXML() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(invalidXml);

        // Act & Assert
        assertDoesNotThrow(() -> exchangeRateService.updateRatesFromCBR());

        verify(exchangeRateProducer, never())
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));
    }

    @Test
    @DisplayName("Should handle RestTemplate exception gracefully")
    void testUpdateRatesFromCBR_RestTemplateException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertDoesNotThrow(() -> exchangeRateService.updateRatesFromCBR());

        verify(exchangeRateProducer, never())
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));
    }

    @Test
    @DisplayName("Should continue processing if one event fails to send to Kafka")
    void testUpdateRatesFromCBR_PartialFailure() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockXmlMultiple);

        // Первый вызов выбрасывает исключение, остальные успешны
        doThrow(new RuntimeException("Kafka error"))
                .doNothing()
                .doNothing()
                .when(exchangeRateProducer)
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));

        // Act & Assert
        assertDoesNotThrow(() -> exchangeRateService.updateRatesFromCBR());

        // Все 3 события должны быть попытаны
        verify(exchangeRateProducer, times(3))
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));
    }

    @Test
    @DisplayName("Should fake update without errors")
    void testFakeUpdateRates() {
        assertDoesNotThrow(() -> exchangeRateService.fakeUpdateRates());
    }

    @Test
    @DisplayName("Should return not null rates map")
    void testGetRates_NotNull() {
        Map<String, String> rates = exchangeRateService.getRates();
        assertNotNull(rates);
        assertThat(rates).isNotNull();
    }

    @Test
    @DisplayName("Should return defensive copy of rates map")
    void testGetRates_ReturnsCopy() {
        // Get rates twice
        Map<String, String> rates1 = exchangeRateService.getRates();
        Map<String, String> rates2 = exchangeRateService.getRates();

        // Should be equal but not the same object
        assertThat(rates1).isEqualTo(rates2);
        assertNotSame(rates1, rates2);
    }

    @Test
    @DisplayName("Should get all rates from database")
    void testGetAllRatesFromDB_Success() {
        // Arrange
        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.50"))
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(2L)
                .code("EUR")
                .rate(new BigDecimal("110.25"))
                .build();

        when(exchangeRateRepository.findAll())
                .thenReturn(List.of(rate1, rate2));

        // Act
        Map<String, BigDecimal> result = exchangeRateService.getAllRatesFromDB();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("USD", new BigDecimal("100.50"));
        assertThat(result).containsEntry("EUR", new BigDecimal("110.25"));
        verify(exchangeRateRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty map when no rates in database")
    void testGetAllRatesFromDB_Empty() {
        // Arrange
        when(exchangeRateRepository.findAll())
                .thenReturn(List.of());

        // Act
        Map<String, BigDecimal> result = exchangeRateService.getAllRatesFromDB();

        // Assert
        assertThat(result).isEmpty();
        verify(exchangeRateRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should handle database exception in getAllRatesFromDB")
    void testGetAllRatesFromDB_Exception() {
        // Arrange
        when(exchangeRateRepository.findAll())
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertDoesNotThrow(() -> exchangeRateService.getAllRatesFromDB());

        Map<String, BigDecimal> result = exchangeRateService.getAllRatesFromDB();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle XML with extra whitespaces")
    void testUpdateRatesFromCBR_WithWhitespaces() {
        // Arrange
        String xmlWithWhitespace = "<?xml version=\"1.0\" encoding=\"Windows-1251\"?>" +
                "<ValCurs>" +
                "<Valute ID=\"R01235\">" +
                "<CharCode>  USD  </CharCode>" +
                "<Value>  100,50  </Value>" +
                "</Valute>" +
                "</ValCurs>";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(xmlWithWhitespace);

        doNothing().when(exchangeRateProducer)
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        ArgumentCaptor<ExchangeRateEvent> captor = ArgumentCaptor.forClass(ExchangeRateEvent.class);
        verify(exchangeRateProducer, times(1))
                .sendExchangeRateEvent(captor.capture());

        ExchangeRateEvent event = captor.getValue();
        // Проверяем, что whitespace был удалён
        assertThat(event.getCode()).isEqualTo("USD");
        assertThat(event.getRate()).isEqualByComparingTo(new BigDecimal("100.50"));
    }

    @Test
    @DisplayName("Should verify correct number of Kafka calls")
    void testUpdateRatesFromCBR_VerifyKafkaInteractions() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockXmlMultiple);

        doNothing().when(exchangeRateProducer)
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        verify(exchangeRateProducer, times(3))
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));
        verify(exchangeRateProducer, never()).sendExchangeRateEvent(null);
    }

    @Test
    @DisplayName("Should handle XML with single element")
    void testUpdateRatesFromCBR_SingleElement() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockXml);

        doNothing().when(exchangeRateProducer)
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        ArgumentCaptor<ExchangeRateEvent> captor = ArgumentCaptor.forClass(ExchangeRateEvent.class);
        verify(exchangeRateProducer, times(1))
                .sendExchangeRateEvent(captor.capture());

        List<ExchangeRateEvent> events = captor.getAllValues();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should verify RestTemplate was called with correct URL")
    void testUpdateRatesFromCBR_VerifyURLCall() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(mockXml);

        doNothing().when(exchangeRateProducer)
                .sendExchangeRateEvent(any(ExchangeRateEvent.class));

        // Act
        exchangeRateService.updateRatesFromCBR();

        // Assert
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, times(1))
                .getForObject(urlCaptor.capture(), eq(String.class));

        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).contains("cbr.ru");
        assertThat(capturedUrl).contains("XML_daily.asp");
        assertThat(capturedUrl).contains("date_req=");
    }
}
