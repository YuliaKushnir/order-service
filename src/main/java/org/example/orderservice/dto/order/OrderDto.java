package org.example.orderservice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.data.enums.OrderStatus;
import org.example.orderservice.data.enums.Priority;
import org.example.orderservice.dto.item.OrderItemDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private String userId;

    private OrderStatus status;
    private Priority priority;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private LocalDateTime executionDate;

    private String managerId;
    private String workerId;

    private String userNote;
    private String internalNote;

    private List<OrderItemDto> items;
}