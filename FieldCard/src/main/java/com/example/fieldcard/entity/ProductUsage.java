package com.example.fieldcard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_usage")
public class ProductUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private PlantProtectionProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pest_id", nullable = false)
    private Pest pest;

    @Column(name = "dosage", columnDefinition = "text")
    private String dosage;

    @Column(name = "application_timing", columnDefinition = "text")
    private String applicationTiming;

    @Column(name = "is_minor_use")
    private boolean isMinorUse = false;

    @Column(name="is_active", nullable = false)
    private boolean isActive = true;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public PlantProtectionProduct getProduct() { return product; }
    public void setProduct(PlantProtectionProduct product) { this.product = product; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public Pest getPest() { return pest; }
    public void setPest(Pest pest) { this.pest = pest; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getApplicationTiming() { return applicationTiming; }
    public void setApplicationTiming(String applicationTiming) { this.applicationTiming = applicationTiming; }
    public boolean isMinorUse() { return isMinorUse; }
    public void setMinorUse(boolean minorUse) { this.isMinorUse = minorUse; }

    @Override
    public String toString() {
        return "ProductUsage [id=" + id + ", product=" + product + ", crop=" + crop + ", pest=" + pest + ", dosage=" + dosage + ", applicationTiming=" + applicationTiming + ", isMinorUse=" + isMinorUse + "]";
    }
}