package com.example.fieldcard.data.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry; // Używamy ogólnego typu Geometry lub Polygon/MultiPolygon

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fields")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name; // np. "Działka za lasem"

    @Column(name = "area_ha", precision = 10, scale = 4)
    private BigDecimal area; // Suma powierzchni działek

    @Column
    private String description;


    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Parcel> parcels = new ArrayList<>();


    @Column(columnDefinition = "geometry(Geometry, 4326)")
    private Geometry border;

    public Field() {}


    public void addParcel(Parcel parcel) {
        parcels.add(parcel);
        parcel.setField(this);
    }

    public void removeParcel(Parcel parcel) {
        parcels.remove(parcel);
        parcel.setField(null);
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Parcel> getParcels() { return parcels; }
    public void setParcels(List<Parcel> parcels) { this.parcels = parcels; }

    public Geometry getBorder() { return border; }
    public void setBorder(Geometry border) { this.border = border; }
}