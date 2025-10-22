package com.example.fieldcard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pest_dictionary")
public class Pest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    public Pest(){

    }
    public Pest(String name){
        this.name = name;
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
    @Override
    public String toString(){
        return "Pest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
