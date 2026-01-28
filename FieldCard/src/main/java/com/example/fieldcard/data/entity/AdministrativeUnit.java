package com.example.fieldcard.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "administrative_units", indexes = {
        @Index(name = "idx_parent_id", columnList = "parentId")
})
public class AdministrativeUnit {
    @Id
    private String id;
    private String name;
    private String type;
    private String parentId;
}