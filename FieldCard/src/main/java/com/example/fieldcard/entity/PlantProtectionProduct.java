package com.example.fieldcard.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "plant_protection_product")
public class PlantProtectionProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sor_id", nullable = false, unique = true)
    private String sorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "permit_number")
    private String permitNumber;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "active_substances_string", columnDefinition = "text")
    private String activeSubstancesString;

    @Column(name = "permit_date")
    private LocalDate permitDate;

    @Column(name = "sales_deadline")
    private LocalDate salesDeadline;

    @Column(name = "use_deadline")
    private LocalDate useDeadline;

    @Column(name = "label_url")
    private String labelUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;


    public PlantProtectionProduct() {
    }

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

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getActiveSubstancesString() {
        return activeSubstancesString;
    }

    public void setActiveSubstancesString(String activeSubstancesString) {
        this.activeSubstancesString = activeSubstancesString;
    }

    public LocalDate getPermitDate() {
        return permitDate;
    }

    public void setPermitDate(LocalDate permitDate) {
        this.permitDate = permitDate;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlantProtectionProduct that = (PlantProtectionProduct) o;
        return Objects.equals(sorId, that.sorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sorId);
    }
}