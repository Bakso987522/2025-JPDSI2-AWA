package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.ApplicationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ApplicationGroupRepository extends JpaRepository<ApplicationGroup, Long> {

    List<ApplicationGroup> findAllByIsActive(boolean isActive);

    @Query("SELECT ag FROM ApplicationGroup ag WHERE ag.name IN :names AND ag.isActive = true")
    List<ApplicationGroup> findAllByNameInAndIsActive(@Param("names") Set<String> names);
}