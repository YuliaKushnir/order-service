package org.example.orderservice.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_items",
        indexes = {
                @Index(name = "idx_item_product_id", columnList = "productId"),
                @Index(name = "idx_item_order_id", columnList = "order_id"),
                @Index(name = "idx_item_color", columnList = "textileColor"),
                @Index(name = "idx_item_size", columnList = "size")
        }
)
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;

    private BigDecimal basePrice;

    private String textileColor;

    private String size;

    private BigDecimal manualTotal;

    private Integer quantity = 1;

    private BigDecimal finalPrice;

    private String comment;

    @ElementCollection
    private List<String> previewUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Print> prints = new ArrayList<>();
}
