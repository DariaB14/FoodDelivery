package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByRestaurantId(Long id);
}
