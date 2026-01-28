package com.example.fieldcard.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "treatment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private String activeSubstance;
    private String targetPest;
    private String dose;

    private boolean isOffLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;
}