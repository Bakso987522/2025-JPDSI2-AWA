package com.example.fieldcard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserStockDto(
        Long id,
        Long productId,
        String productName,
        String manufacturer,
        BigDecimal quantity,
        String unit,
        String batchNumber,
        LocalDate expirationDate
) {}