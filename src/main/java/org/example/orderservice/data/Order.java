package org.example.orderservice.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.data.enums.OrderStatus;
import org.example.orderservice.data.enums.Priority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user_id", columnList = "userId"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created_at", columnList = "createdAt"),
        @Index(name = "idx_order_worker_id", columnList = "workerId"),
        @Index(name = "idx_order_manager_id", columnList = "managerId")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    private LocalDateTime executionDate;

    private LocalDateTime deadline;

    private String managerId;
    private String workerId;

    private String userNote;
    private String internalNote;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}
