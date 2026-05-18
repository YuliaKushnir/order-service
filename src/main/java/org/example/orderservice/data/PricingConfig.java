package org.example.orderservice.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "pricing_config")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal textileLightMultiplier;
    private BigDecimal textileDarkMultiplier;

    private BigDecimal fullColorLightExtra;
    private BigDecimal fullColorDarkExtra;

    private BigDecimal singleColorStep;

    private Integer maxColors;
}