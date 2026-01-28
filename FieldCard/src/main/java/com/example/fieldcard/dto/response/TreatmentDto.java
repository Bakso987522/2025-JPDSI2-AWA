package com.example.fieldcard.dto.response;

import com.example.fieldcard.dto.request.TreatmentItemDto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TreatmentDto {
    private Long id;
    private LocalDate date;
    private String fieldName;
    private List<TreatmentItemDto> items;
}