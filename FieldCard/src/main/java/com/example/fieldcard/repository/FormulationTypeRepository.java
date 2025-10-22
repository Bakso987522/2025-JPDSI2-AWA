package com.example.fieldcard.repository;

import com.example.fieldcard.entity.FormulationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormulationTypeRepository extends JpaRepository<FormulationType, Long> {
}
