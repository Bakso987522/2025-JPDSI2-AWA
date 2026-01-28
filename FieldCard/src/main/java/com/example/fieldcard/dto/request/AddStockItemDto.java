package com.example.fieldcard.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddStockItemDto(
        Long productId,
        BigDecimal quantity,
        String unit,
        String batchNumber,
        LocalDate expirationDate,
        LocalDate purchaseDate
) {}