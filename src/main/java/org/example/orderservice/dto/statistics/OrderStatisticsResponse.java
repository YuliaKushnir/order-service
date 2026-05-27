package org.example.orderservice.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatisticsResponse {

    private BigDecimal total;

    private List<OrderDto> orders;
}