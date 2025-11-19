package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findAllByIsActive(boolean isActive);
}
