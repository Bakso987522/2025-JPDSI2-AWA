package com.example.fieldcard.dto.response;

public class ProductUsageDto {
    private String cropName;
    private String pestName;
    private String dose;


    public ProductUsageDto(String cropName, String pestName, String dose) {
        this.cropName = cropName;
        this.pestName = pestName;
        this.dose = dose;
    }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getPestName() { return pestName; }
    public void setPestName(String pestName) { this.pestName = pestName; }

    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }

}