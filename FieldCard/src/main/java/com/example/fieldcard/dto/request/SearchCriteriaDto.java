package com.example.fieldcard.dto.request;

public class SearchCriteriaDto {
    private String query;
    private String cropName;
    private String pestName;
    private String activeSubstance;
    private String productType;

    public SearchCriteriaDto() {

    }

    public SearchCriteriaDto(String query, String copName, String pestName, String activeSubstance, String productType) {
        this.query = query;
        this.cropName = copName;
        this.pestName = pestName;
        this.activeSubstance = activeSubstance;
        this.productType = productType;
    }
    public String getQuery() {return  query;}
    public void setQuery(String query) {this.query = query;}
    public String getCropName() {return cropName;}
    public void setCropName(String cropName) {this.cropName = cropName;}
    public String getPestName() {return pestName;}
    public void setPestName(String pestName) {this.pestName = pestName;}
    public String getActiveSubstance() {return activeSubstance;}
    public void setActiveSubstance(String activeSubstance) {this.activeSubstance = activeSubstance;}
    public String getProductType() {return productType;}
    public void setProductType(String productType) {this.productType = productType;}
}