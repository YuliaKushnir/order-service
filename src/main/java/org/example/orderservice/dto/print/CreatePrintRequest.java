package org.example.orderservice.dto.print;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePrintRequest {

    private Long typeId;
    private String typeName;

    private String size;
    private String placement;

    private Integer quantity;

    private BigDecimal price;
    private BigDecimal manualTotal;

    private Integer colorCount;

    private List<String> colors;

    /** url вже існуючих файлів */
    private List<String> fileUrls;

    /** нові файли */
    private List<MultipartFile> filesForPrint;
}