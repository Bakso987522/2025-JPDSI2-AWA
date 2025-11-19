package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.Pest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PestRepository extends JpaRepository<Pest, Long> {
    List<Pest> findAllByIsActive(Boolean isActive);
}
