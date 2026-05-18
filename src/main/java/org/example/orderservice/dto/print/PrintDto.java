package org.example.orderservice.dto.print;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrintDto {

    private Long id;

    private Long typeId;
    private String typeName;

    private String size;
    private String placement;

    private Integer quantity;

    private BigDecimal price;
    private BigDecimal manualTotal;

    private Integer colorCount;

    private List<String> colors;

    private List<String> fileUrls;
}