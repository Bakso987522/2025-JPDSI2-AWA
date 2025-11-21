package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.Pest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PestRepository extends JpaRepository<Pest, Long> {
    List<Pest> findAllByIsActive(Boolean isActive);
    @Query(value = """
    SELECT * FROM pest_dictionary 
    WHERE is_active = true AND (name ILIKE :query || '%' OR similarity(name, :query) > 0.1)
    ORDER BY CASE WHEN name ILIKE :query || '%' THEN 0 ELSE 1 END ASC, similarity(name, :query) DESC, name ASC
    LIMIT 10
""", nativeQuery = true)
    List<Pest> findSmartSuggestions(@Param("query") String query);
}
