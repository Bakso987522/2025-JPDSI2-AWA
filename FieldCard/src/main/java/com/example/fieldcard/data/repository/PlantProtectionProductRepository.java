package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.PlantProtectionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantProtectionProductRepository extends
        JpaRepository<PlantProtectionProduct, Long>,
        JpaSpecificationExecutor<PlantProtectionProduct> {

    Optional<PlantProtectionProduct> findBySorId(String sorId);

    List<PlantProtectionProduct> findAllByIsActive(boolean isActive);

    @Query("SELECT p FROM PlantProtectionProduct p " +
            "LEFT JOIN FETCH p.productTypes " +
            "LEFT JOIN FETCH p.activeSubstances")
    List<PlantProtectionProduct> findAllWithProductTypesAndSubstances();

    @Query(value = """
        SELECT * FROM plant_protection_product
        WHERE similarity(name, :query) > 0.2
        ORDER BY similarity(name, :query) DESC
        LIMIT 5
    """, nativeQuery = true)
    List<PlantProtectionProduct> findTop5SimilarProducts(@Param("query") String query);
}