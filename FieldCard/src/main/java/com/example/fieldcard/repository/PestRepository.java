package com.example.fieldcard.repository;

import com.example.fieldcard.entity.Pest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PestRepository extends JpaRepository<Pest, Long> {
}
