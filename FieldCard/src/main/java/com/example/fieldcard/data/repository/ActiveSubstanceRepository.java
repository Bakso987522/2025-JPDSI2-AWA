package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.ActiveSubstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveSubstanceRepository extends JpaRepository<ActiveSubstance, Long> {
    List<ActiveSubstance> findAllByIsActive(boolean isActive);
}
