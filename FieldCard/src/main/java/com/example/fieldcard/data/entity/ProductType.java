package com.example.fieldcard.data.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "product_type")
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;


    @ManyToMany(mappedBy = "productTypes")
    private Set<PlantProtectionProduct> products = new HashSet<>();

    public ProductType() {
    }

    public ProductType(String name) {
        this.name = name;
        this.isActive = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public Set<PlantProtectionProduct> getProducts() { return products; }
    public void setProducts(Set<PlantProtectionProduct> products) { this.products = products; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductType that = (ProductType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}