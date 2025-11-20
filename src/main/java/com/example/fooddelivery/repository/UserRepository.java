package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    List<User> findByRole(UserRole role);
    boolean existsByEmail(String email);
}
