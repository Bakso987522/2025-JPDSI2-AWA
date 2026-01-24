package com.example.fieldcard.dto.response;

import java.util.List;

public class ProductSearchResultDto {
    private Long id;
    private String sorId;
    private String name;
    private List<String> activeSubstance;
    private List<String> types;
    private List<String> crops;
    private List<String> pests;
    private String manufacturer;
    public ProductSearchResultDto() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public List<String> getActiveSubstance() {
        return activeSubstance;
    }
    public void setActiveSubstance(List<String> activeSubstance) {
        this.activeSubstance = activeSubstance;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    public List<String> getType() {
        return  types;
    }
    public void  setType(List<String> types) {
        this.types = types;
    }
    public List<String> getCrops() {
        return  crops;
    }
    public void  setCrops(List<String> crops) {
        this.crops = crops;
    }
    public List<String> getPests() {
        return   pests;
    }
    public  void setPests(List<String> pests) {
        this.pests = pests;
    }
}
