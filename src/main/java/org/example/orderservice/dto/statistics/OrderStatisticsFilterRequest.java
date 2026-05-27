package org.example.orderservice.dto.statistics;

import lombok.Data;
import org.example.orderservice.data.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderStatisticsFilterRequest {

    private String managerId;

    private String workerId;

    private List<OrderStatus> statuses;

    private LocalDate dateFrom;

    private LocalDate dateTo;
}