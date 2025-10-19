package com.example.fieldcard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDto {
    private AttributesDto attributes;

    public AttributesDto getAttributes() {
        return attributes;
    }
    public void setAttributes(AttributesDto attributes) {
        this.attributes = attributes;
    }
}
