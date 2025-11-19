package com.example.fieldcard.repository;

import com.example.fieldcard.entity.PlantProtectionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantProtectionProductRepository extends JpaRepository<PlantProtectionProduct, Long> {

    Optional<PlantProtectionProduct> findBySorId(String sorId);

    List<PlantProtectionProduct> findAllByIsActive(boolean isActive);

    @Query("SELECT p FROM PlantProtectionProduct p " +
            "LEFT JOIN FETCH p.productTypes " +
            "LEFT JOIN FETCH p.activeSubstances")
    List<PlantProtectionProduct> findAllWithProductTypesAndSubstances();
}