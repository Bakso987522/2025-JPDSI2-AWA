package com.example.fieldcard.repository;

import com.example.fieldcard.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    List<ProductType> findAllByIsActive(boolean isActive);

    @Query("SELECT ft FROM ProductType ft WHERE ft.name IN :names AND ft.isActive = true")
    List<ProductType> findAllByNameInAndIsActive(@Param("names") Set<String> names);
}