package org.example.orderservice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.data.enums.OrderStatus;
import org.example.orderservice.dto.item.UpdateOrderItemRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderFullRequest {

    private String userId;

    private OrderStatus status;
    private LocalDateTime deadline;
    private LocalDateTime executionDate;

    private String managerId;
    private String workerId;

    private String userNote;
    private String internalNote;

    private BigDecimal totalPrice;

    private List<UpdateOrderItemRequest> items;
}
