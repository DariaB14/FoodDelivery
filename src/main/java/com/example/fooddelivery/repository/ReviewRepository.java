package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Review;
import com.example.fooddelivery.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserId(Long userId);

    @Query("select r from Review r where r.restaurant.id = :restaurantId order by r.createdAt desc limit 50")
    List<Review> findLast50ReviewsByRestaurantId(@Param("restaurantId") Long restaurantId);

    boolean existsByOrderId(Long orderId);
    Optional<Review> findByIdAndUserId(Long id, Long userId);
    List<Review> findAllByRestaurantIdAndStatus(Long restaurantId, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    BigDecimal calculateAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);
}
