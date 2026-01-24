package com.example.fieldcard.dto.request;

import java.util.List;

public class SearchCriteriaDto {
    private String query;
    private List<String> cropName;
    private List<String> pestName;
    private List<String> activeSubstance;
    private List<String> productType;

    public SearchCriteriaDto() {
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<String> getCropName() { return cropName; }
    public void setCropName(List<String> cropName) { this.cropName = cropName; }

    public List<String> getPestName() { return pestName; }
    public void setPestName(List<String> pestName) { this.pestName = pestName; }

    public List<String> getActiveSubstance() { return activeSubstance; }
    public void setActiveSubstance(List<String> activeSubstance) { this.activeSubstance = activeSubstance; }

    public List<String> getProductType() { return productType; }
    public void setProductType(List<String> productType) { this.productType = productType; }
}