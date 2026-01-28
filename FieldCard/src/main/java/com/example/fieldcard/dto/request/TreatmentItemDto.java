package com.example.fieldcard.dto.request;

import lombok.Data;

@Data
public class TreatmentItemDto {
    private String productName;
    private String activeSubstance;
    private String targetPest;
    private String dose;
    private boolean isOffLabel;
}