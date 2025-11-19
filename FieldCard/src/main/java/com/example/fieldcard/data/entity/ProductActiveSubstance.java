package com.example.fieldcard.data.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "product_active_substances")
public class ProductActiveSubstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private PlantProtectionProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_substance_id")
    private ActiveSubstance activeSubstance;

    @Column(name = "content")
    private String content;

    public ProductActiveSubstance() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PlantProtectionProduct getProduct() { return product; }
    public void setProduct(PlantProtectionProduct product) { this.product = product; }
    public ActiveSubstance getActiveSubstance() { return activeSubstance; }
    public void setActiveSubstance(ActiveSubstance activeSubstance) { this.activeSubstance = activeSubstance; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductActiveSubstance that = (ProductActiveSubstance) o;
        return Objects.equals(activeSubstance, that.activeSubstance) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeSubstance, content);
    }
}