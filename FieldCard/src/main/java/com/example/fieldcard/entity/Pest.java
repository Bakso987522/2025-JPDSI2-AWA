package com.example.fieldcard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pest_dictionary")
public class Pest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name="is_active", nullable = false)
    private boolean isActive = true;

    public Pest(){

    }
    public Pest(String name){
        this.name = name;
        this.isActive = true;
    }
    public long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public boolean isActive(){
        return isActive;
    }
    public void setActive(boolean active){
        isActive = active;
    }
    @Override
    public String toString(){
        return "Pest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
