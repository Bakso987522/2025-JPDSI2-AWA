package com.example.fieldcard.data.repository;

import com.example.fieldcard.data.entity.User;
import com.example.fieldcard.data.entity.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {
    List<UserStock> findAllByUser(User user);

    Optional<UserStock> findByIdAndUser(Long id, User user);
}