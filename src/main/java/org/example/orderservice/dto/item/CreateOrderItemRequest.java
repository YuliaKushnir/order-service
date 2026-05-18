package org.example.orderservice.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.dto.print.CreatePrintRequest;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderItemRequest {

    private Long productId;
    private String productName;

    private BigDecimal basePrice;

    private String textileColor;
    private String size;

    private Integer quantity;

    private BigDecimal manualTotal;
    private BigDecimal finalPrice;

    private String comment;

    private List<String> previewUrls;
    private List<MultipartFile> previewFiles;

    private List<CreatePrintRequest> prints;
}