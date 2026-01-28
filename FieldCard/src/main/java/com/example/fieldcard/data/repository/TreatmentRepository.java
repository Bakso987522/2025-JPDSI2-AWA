package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    @Query("SELECT DISTINCT t FROM Treatment t " +
            "JOIN FETCH t.field f " +
            "LEFT JOIN FETCH t.items " +
            "WHERE f.user.email = :email " +
            "ORDER BY t.date DESC")
    List<Treatment> findAllByUserEmail(@Param("email") String email);
}