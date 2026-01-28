package com.example.fieldcard.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TreatmentRequestDto {
    private LocalDate date;
    private Long fieldId;
    private String description;
    private List<TreatmentItemDto> items;
}