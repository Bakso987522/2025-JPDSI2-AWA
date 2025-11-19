package com.example.fieldcard.data.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

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

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "product_product_types",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "product_type_id")
    )
    private Set<ProductType> productTypes = new HashSet<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ProductActiveSubstance> activeSubstances = new HashSet<>();

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

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductUsage> usages = new ArrayList<>();
    public PlantProtectionProduct() {
    }

    public List<ProductUsage> getUsages() {
        return usages;
    }

    public void setUsages(List<ProductUsage> usages) {
        this.usages = usages;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSorId() { return sorId; }
    public void setSorId(String sorId) { this.sorId = sorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getPermitNumber() { return permitNumber; }
    public void setPermitNumber(String permitNumber) { this.permitNumber = permitNumber; }
    public Set<ProductType> getProductTypes() { return productTypes; }
    public void setProductTypes(Set<ProductType> productTypes) { this.productTypes = productTypes; }
    public Set<ProductActiveSubstance> getActiveSubstances() { return activeSubstances; }
    public void setActiveSubstances(Set<ProductActiveSubstance> activeSubstances) { this.activeSubstances = activeSubstances; }
    public LocalDate getPermitDate() { return permitDate; }
    public void setPermitDate(LocalDate permitDate) { this.permitDate = permitDate; }
    public LocalDate getSalesDeadline() { return salesDeadline; }
    public void setSalesDeadline(LocalDate salesDeadline) { this.salesDeadline = salesDeadline; }
    public LocalDate getUseDeadline() { return useDeadline; }
    public void setUseDeadline(LocalDate useDeadline) { this.useDeadline = useDeadline; }
    public String getLabelUrl() { return labelUrl; }
    public void setLabelUrl(String labelUrl) { this.labelUrl = labelUrl; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

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