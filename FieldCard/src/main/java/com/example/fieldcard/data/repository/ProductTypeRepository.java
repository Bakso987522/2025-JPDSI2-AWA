package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.ProductType;
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
    @Query(value = """
    SELECT * FROM product_type 
    WHERE is_active = true AND (name ILIKE :query || '%' OR similarity(name, :query) > 0.1)
    ORDER BY CASE WHEN name ILIKE :query || '%' THEN 0 ELSE 1 END ASC, similarity(name, :query) DESC, name ASC
    LIMIT 10
""", nativeQuery = true)
    List<ProductType> findSmartSuggestions(@Param("query") String query);
}