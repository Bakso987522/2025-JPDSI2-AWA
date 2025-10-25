package com.example.fieldcard.entity;

import jakarta.persistence.*;

@Entity
@Table(name ="active_substance")
public class ActiveSubstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    public  ActiveSubstance() {
    }
    public ActiveSubstance(String name) {
        this.name = name;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return "ActiveSubstance{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
