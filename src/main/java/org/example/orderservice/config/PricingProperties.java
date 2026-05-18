package org.example.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "pricing")
public class PricingProperties {

    private BigDecimal textileLightMultiplier = BigDecimal.ONE;
    private BigDecimal textileDarkMultiplier = new BigDecimal("1.2");

    private BigDecimal fullColorLightExtra = new BigDecimal("50");
    private BigDecimal fullColorDarkExtra = new BigDecimal("100");

    private BigDecimal singleColorBaseMultiplier = BigDecimal.ONE;
    private BigDecimal singleColorStep = new BigDecimal("0.5");

    private int maxColors = 5;

    private Map<String, BigDecimal> sizePrices = Map.of(
            "A6", new BigDecimal("30"),
            "A5", new BigDecimal("60"),
            "A4", new BigDecimal("100"),
            "A3", new BigDecimal("180")
    );

}