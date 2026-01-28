package com.example.fieldcard.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FieldDto(
        Long id,
        String name,
        BigDecimal area,
        String description,
        List<String> parcelNumbers
) {}