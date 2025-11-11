package com.example.fieldcard.repository;

import com.example.fieldcard.entity.ActiveSubstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveSubstanceRepository extends JpaRepository<ActiveSubstance, Long> {
    List<ActiveSubstance> findAllByIsActive(boolean isActive);
}
