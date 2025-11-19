package com.example.fieldcard.data.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "application_group")
public class ApplicationGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private  String name;

    @Column(name="is_active", nullable = false)
    private boolean isActive = true;



    public ApplicationGroup() {
    }

    public ApplicationGroup(String name) {
        this.name = name;
        this.isActive = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }


    @Override
    public String toString(){
        return "ApplicationGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationGroup that = (ApplicationGroup) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}