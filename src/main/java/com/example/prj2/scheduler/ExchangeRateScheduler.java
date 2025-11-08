package com.example.prj2.scheduler;

import com.example.prj2.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateDaily() {
        log.info("Scheduled: Updating rates from CBR");
        exchangeRateService.updateRatesFromCBR();
    }

    @Scheduled(cron = "0 1 0 * * ?")
    public void fakeUpdateDaily() {
        log.info("Scheduled: Fake update");
        exchangeRateService.fakeUpdateRates();
    }
}
