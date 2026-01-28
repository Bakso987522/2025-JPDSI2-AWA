package com.example.fieldcard.dto.request;

import java.math.BigDecimal;
import java.util.List;
public record CreateFieldDto(
        String name,
        BigDecimal area,
        String description,
        List<String> parcelIds

) {
}
