package com.example.fieldcard.dto.response;

public class ProductSearchResultDto {
    private String sorId;
    private String name;
    private String activeSubstance;
    private String manufacturer;
    public ProductSearchResultDto() {
    }
    public  ProductSearchResultDto(String sorId, String name, String activeSubstance, String manufacturer) {
        this.sorId = sorId;
        this.name = name;
        this.activeSubstance = activeSubstance;
        this.manufacturer = manufacturer;
    }
    public String getSorId() {
        return sorId;
    }
    public void setSorId(String sorId) {
        this.sorId = sorId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getActiveSubstance() {
        return activeSubstance;
    }
    public void setActiveSubstance(String activeSubstance) {
        this.activeSubstance = activeSubstance;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
