package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.AdministrativeUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnit, String> {
    List<AdministrativeUnit> findAllByParentId(String parentId);
}
