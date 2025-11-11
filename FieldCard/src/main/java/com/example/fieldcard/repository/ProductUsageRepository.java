package com.example.fieldcard.repository;

import com.example.fieldcard.entity.ProductUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- Upewnij się, że ten import jest
import org.springframework.stereotype.Repository;

import java.util.List; // <-- Upewnij się, że ten import jest

@Repository
public interface ProductUsageRepository extends JpaRepository<ProductUsage, Long> {

    /**
     * TO JEST BRAKUJĄCA METODA
     * Pobiera wszystkie zastosowania, ale od razu dociąga (JOIN FETCH)
     * powiązane encje produktu, uprawy i agrofaga w JEDNYM zapytaniu.
     * To kluczowe dla wydajności.
     */
    @Query("SELECT pu FROM ProductUsage pu " +
            "JOIN FETCH pu.product p " +
            "JOIN FETCH pu.crop c " +
            "JOIN FETCH pu.pest pe")
    List<ProductUsage> findAllWithRelationships();
}