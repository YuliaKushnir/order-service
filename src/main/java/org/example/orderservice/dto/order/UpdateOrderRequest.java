package org.example.orderservice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.data.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {

    private OrderStatus status;
    private LocalDateTime deadline;
    private LocalDateTime executionDate;

    private String managerId;
    private String workerId;

    private String internalNote;
}