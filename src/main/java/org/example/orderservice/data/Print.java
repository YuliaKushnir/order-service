package org.example.orderservice.data;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "prints",
        indexes = {
                @Index(name = "idx_print_order_item", columnList = "order_item_id"),
                @Index(name = "idx_print_type_id", columnList = "typeId"),
                @Index(name = "idx_print_type_name", columnList = "typeName"),
                @Index(name = "idx_print_size", columnList = "size"),
                @Index(name = "idx_print_placement", columnList = "placement")
        }
)
public class Print {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeName;
    private Long typeId;

    private String size;
    private String placement;

    private Integer quantity = 1;

    private BigDecimal price;
    private BigDecimal manualTotal;

    private Integer colorCount;

    @ElementCollection
    private List<String> colors;

    @ElementCollection
    private List<String> fileUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;
}