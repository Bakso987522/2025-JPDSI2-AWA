package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.Field;
import com.example.fieldcard.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findAllByUser(User user);
}
