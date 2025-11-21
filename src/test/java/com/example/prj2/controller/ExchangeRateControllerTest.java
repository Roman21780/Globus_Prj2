package com.example.prj2.controller;

import com.example.prj2.service.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("GET /rates возвращает список курсов валют")
    void testGetRates() throws Exception {
        Map<String, String> rates = Map.of("USD", "100.5", "EUR", "110.2");
        when(exchangeRateService.getRates()).thenReturn(rates);

        mockMvc.perform(get("/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD").value("100.5"))
                .andExpect(jsonPath("$.EUR").value("110.2"));

        verify(exchangeRateService, times(1)).getRates();
    }

    @Test
    @DisplayName("POST /rates/update вызывает обновление и возвращает 'Updated'")
    void testUpdateRates() throws Exception {
        doNothing().when(exchangeRateService).updateRatesFromCBR();

        mockMvc.perform(post("/rates/update")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated"));

        verify(exchangeRateService, times(1)).updateRatesFromCBR();
    }

    @Test
    @DisplayName("POST /rates/fake-update вызывает фейковое обновление и возвращает 'Fake updated'")
    void testFakeUpdate() throws Exception {
        doNothing().when(exchangeRateService).fakeUpdateRates();

        mockMvc.perform(post("/rates/fake-update")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Fake updated"));

        verify(exchangeRateService, times(1)).fakeUpdateRates();
    }
}
