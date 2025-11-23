package com.example.prj2.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExchangeRate Entity Tests")
class ExchangeRateTest {

    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        exchangeRate = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create ExchangeRate with builder")
    void testBuilder() {
        ExchangeRate rate = ExchangeRate.builder()
                .id(1L)
                .code("EUR")
                .rate(new BigDecimal("110.2500"))
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(rate.getId()).isEqualTo(1L);
        assertThat(rate.getCode()).isEqualTo("EUR");
        assertThat(rate.getRate()).isEqualByComparingTo(new BigDecimal("110.2500"));
        assertThat(rate.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should get id")
    void testGetId() {
        assertThat(exchangeRate.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should set id")
    void testSetId() {
        exchangeRate.setId(5L);
        assertThat(exchangeRate.getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should get code")
    void testGetCode() {
        assertThat(exchangeRate.getCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Should set code")
    void testSetCode() {
        exchangeRate.setCode("GBP");
        assertThat(exchangeRate.getCode()).isEqualTo("GBP");
    }

    @Test
    @DisplayName("Should get rate")
    void testGetRate() {
        assertThat(exchangeRate.getRate()).isEqualByComparingTo(new BigDecimal("100.5000"));
    }

    @Test
    @DisplayName("Should set rate")
    void testSetRate() {
        BigDecimal newRate = new BigDecimal("105.7500");
        exchangeRate.setRate(newRate);
        assertThat(exchangeRate.getRate()).isEqualByComparingTo(newRate);
    }

    @Test
    @DisplayName("Should get updatedAt")
    void testGetUpdatedAt() {
        assertThat(exchangeRate.getUpdatedAt()).isNotNull();
        assertThat(exchangeRate.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should set updatedAt")
    void testSetUpdatedAt() {
        LocalDateTime newTime = LocalDateTime.now().minusHours(1);
        exchangeRate.setUpdatedAt(newTime);
        assertThat(exchangeRate.getUpdatedAt()).isEqualTo(newTime);
    }

    @Test
    @DisplayName("Should create ExchangeRate with no-args constructor")
    void testNoArgsConstructor() {
        ExchangeRate rate = new ExchangeRate();
        assertThat(rate.getId()).isNull();
        assertThat(rate.getCode()).isNull();
        assertThat(rate.getRate()).isNull();
        assertThat(rate.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should create ExchangeRate with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ExchangeRate rate = new ExchangeRate(
                2L,
                "CHF",
                new BigDecimal("95.1500"),
                now
        );

        assertThat(rate.getId()).isEqualTo(2L);
        assertThat(rate.getCode()).isEqualTo("CHF");
        assertThat(rate.getRate()).isEqualByComparingTo(new BigDecimal("95.1500"));
        assertThat(rate.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should test equals with same values")
    void testEquals_SameValues() {
        LocalDateTime now = LocalDateTime.now();

        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        assertThat(rate1).isEqualTo(rate2);
    }

    @Test
    @DisplayName("Should test equals with different ids")
    void testEquals_DifferentIds() {
        LocalDateTime now = LocalDateTime.now();

        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(2L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        assertThat(rate1).isNotEqualTo(rate2);
    }

    @Test
    @DisplayName("Should test equals with different codes")
    void testEquals_DifferentCodes() {
        LocalDateTime now = LocalDateTime.now();

        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(1L)
                .code("EUR")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        assertThat(rate1).isNotEqualTo(rate2);
    }

    @Test
    @DisplayName("Should test equals with different rates")
    void testEquals_DifferentRates() {
        LocalDateTime now = LocalDateTime.now();

        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("105.5000"))
                .updatedAt(now)
                .build();

        assertThat(rate1).isNotEqualTo(rate2);
    }

    @Test
    @DisplayName("Should test canEqual")
    void testCanEqual() {
        ExchangeRate rate = new ExchangeRate();
        assertThat(rate.canEqual(new ExchangeRate())).isTrue();
        assertThat(rate.canEqual("some string")).isFalse();
        assertThat(rate.canEqual(null)).isFalse();
    }

    @Test
    @DisplayName("Should test hashCode with same values")
    void testHashCode_SameValues() {
        LocalDateTime now = LocalDateTime.now();

        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(now)
                .build();

        assertThat(rate1.hashCode()).isEqualTo(rate2.hashCode());
    }

    @Test
    @DisplayName("Should test hashCode with different values")
    void testHashCode_DifferentValues() {
        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(LocalDateTime.now())
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(2L)
                .code("EUR")
                .rate(new BigDecimal("110.5000"))
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(rate1.hashCode()).isNotEqualTo(rate2.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void testToString() {
        ExchangeRate rate = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .updatedAt(LocalDateTime.now())
                .build();

        String toString = rate.toString();

        assertThat(toString).contains("ExchangeRate(")
                .contains("id=1")
                .contains("code=USD")
                .contains("rate=100.5000");
    }

    @Test
    @DisplayName("Should handle rate with different scales")
    void testRateWithDifferentScales() {
        ExchangeRate rate1 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.50"))
                .build();

        ExchangeRate rate2 = ExchangeRate.builder()
                .id(1L)
                .code("USD")
                .rate(new BigDecimal("100.5000"))
                .build();

        // BigDecimal.equals() зависит от масштаба, поэтому используем compareTo
        assertThat(rate1.getRate().compareTo(rate2.getRate())).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle null values in fields")
    void testNullValues() {
        ExchangeRate rate = new ExchangeRate(
                null,      // id
                null,      // code
                null,      // rate
                null       // updatedAt
        );

        assertThat(rate.getId()).isNull();
        assertThat(rate.getCode()).isNull();
        assertThat(rate.getRate()).isNull();
        assertThat(rate.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle very large rate values")
    void testLargeRateValues() {
        BigDecimal largeRate = new BigDecimal("999999.9999");
        exchangeRate.setRate(largeRate);
        assertThat(exchangeRate.getRate()).isEqualByComparingTo(largeRate);
    }

    @Test
    @DisplayName("Should handle very small rate values")
    void testSmallRateValues() {
        BigDecimal smallRate = new BigDecimal("0.0001");
        exchangeRate.setRate(smallRate);
        assertThat(exchangeRate.getRate()).isEqualByComparingTo(smallRate);
    }

    @Test
    @DisplayName("Should validate code is not empty")
    void testCodeNotEmpty() {
        exchangeRate.setCode("EUR");
        assertThat(exchangeRate.getCode()).isNotEmpty();
        assertThat(exchangeRate.getCode()).hasSize(3);
    }

    @Test
    @DisplayName("Should validate rate is not negative")
    void testRateNotNegative() {
        BigDecimal positiveRate = new BigDecimal("100.5000");
        exchangeRate.setRate(positiveRate);
        assertThat(exchangeRate.getRate()).isPositive();
    }
}
