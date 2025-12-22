package com.example.fieldcard.dto.response;

import java.time.LocalDate;
import java.util.List;

public class ProductDetailsDto {
    private String sorId;
    private String name;
    private String manufacturer;
    private String permitNumber;
    private LocalDate permitDate;
    private LocalDate useDeadline;
    private String labelUrl;
    private List<String> activeSubstances;
    private List<ProductUsageDto> usages;

    public String getSorId() { return sorId; }
    public void setSorId(String sorId) { this.sorId = sorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getPermitNumber() { return permitNumber; }
    public void setPermitNumber(String permitNumber) { this.permitNumber = permitNumber; }

    public LocalDate getPermitDate() { return permitDate; }
    public void setPermitDate(LocalDate permitDate) { this.permitDate = permitDate; }

    public LocalDate getUseDeadline() { return useDeadline; }
    public void setUseDeadline(LocalDate useDeadline) { this.useDeadline = useDeadline; }

    public String getLabelUrl() { return labelUrl; }
    public void setLabelUrl(String labelUrl) { this.labelUrl = labelUrl; }

    public List<String> getActiveSubstances() { return activeSubstances; }
    public void setActiveSubstances(List<String> activeSubstances) { this.activeSubstances = activeSubstances; }

    public List<ProductUsageDto> getUsages() { return usages; }
    public void setUsages(List<ProductUsageDto> usages) { this.usages = usages; }
}