package com.example.fieldcard.repository;

import com.example.fieldcard.entity.ApplicationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationGroupRepository extends JpaRepository<ApplicationGroup, Long> {
}
