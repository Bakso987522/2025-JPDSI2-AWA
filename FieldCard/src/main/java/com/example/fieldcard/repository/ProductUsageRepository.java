package com.example.fieldcard.repository;

import com.example.fieldcard.entity.ProductUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductUsageRepository extends JpaRepository<ProductUsage, Long> {


    @Query("SELECT pu FROM ProductUsage pu " +
            "LEFT JOIN FETCH pu.product p " +
            "LEFT JOIN FETCH pu.crop c " +
            "LEFT JOIN FETCH pu.pest pe")
    List<ProductUsage> findAllWithRelationships();
}