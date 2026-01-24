package com.example.fieldcard.dto.response;

import java.time.LocalDate;
import java.util.List;

public class ProductDetailsDto {
    private Long id;
    private String sorId;
    private String name;
    private String manufacturer;
    private List<String> type;
    private String permitNumber;
    private LocalDate salesDeadline;
    private LocalDate useDeadline;
    private String labelUrl;
    private List<String> activeSubstances;
    private List<ProductUsageDto> usages;
    private List<String> crops;
    private List<String> pests;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public LocalDate getSalesDeadline() {
        return salesDeadline;
    }

    public void setSalesDeadline(LocalDate salesDeadline) {
        this.salesDeadline = salesDeadline;
    }

    public LocalDate getUseDeadline() {
        return useDeadline;
    }

    public void setUseDeadline(LocalDate useDeadline) {
        this.useDeadline = useDeadline;
    }

    public String getLabelUrl() {
        return labelUrl;
    }

    public void setLabelUrl(String labelUrl) {
        this.labelUrl = labelUrl;
    }

    public List<String> getActiveSubstances() {
        return activeSubstances;
    }

    public void setActiveSubstances(List<String> activeSubstances) {
        this.activeSubstances = activeSubstances;
    }

    public List<ProductUsageDto> getUsages() {
        return usages;
    }

    public void setUsages(List<ProductUsageDto> usages) {
        this.usages = usages;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<String> getCrops() {
        return crops;
    }

    public void setCrops(List<String> crops) {
        this.crops = crops;
    }

    public List<String> getPests() {
        return pests;
    }

    public void setPests(List<String> pests) {
        this.pests = pests;
    }
}