package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.enums.CuisineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r " +
            "WHERE (:cuisine IS NULL OR r.cuisineType = :cuisine) " +
            "AND (:minRating IS NULL OR r.rating >= :minRating) " +
            "ORDER BY r.rating DESC")
    List<Restaurant> findByCuisineAndRating(@Param("cuisine") CuisineType cuisine,
                                            @Param("minRating") Double minRating);
}
