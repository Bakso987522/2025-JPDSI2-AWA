package com.example.fieldcard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceListDto {
    private List<ResourceDto> data;

    public List<ResourceDto> getData() {
        return data;
    }
    public void setData(List<ResourceDto> data) {
        this.data = data;
    }
}
