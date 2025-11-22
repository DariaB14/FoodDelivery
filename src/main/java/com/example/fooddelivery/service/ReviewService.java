package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.ReviewMapper;
import com.example.fooddelivery.dto.request.ReviewRequest;
import com.example.fooddelivery.dto.response.ReviewResponse;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.Review;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.enums.ReviewStatus;
import com.example.fooddelivery.exception.exceptions.EntityNotFoundException;
import com.example.fooddelivery.exception.exceptions.ReviewException;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;
    private final SpamService spamService;

    public ReviewResponse createReview(ReviewRequest request, Long userId){
        if (reviewRepository.existsByOrderId(request.getOrderId())) {
            throw new ReviewException("Review for this order already exists");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", request.getOrderId())));

        if(!order.getUser().getId().equals(userId)){
            throw new ReviewException("This order belongs to other user");
        }

        if(!order.getStatus().equals(OrderStatus.DELIVERED)){
            throw new ReviewException("Review can be left only for delivered orders");
        }

        Long restaurantId = order.getCart().getRestaurant().getId();
        ReviewStatus status = spamService.moderateReview(request, userId, restaurantId);

        Review review = reviewMapper.toEntity(request);
        review.setUser(order.getUser());
        review.setRestaurant(order.getCart().getRestaurant());
        review.setOrder(order);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(status);

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    public ReviewResponse updateReview(Long id, ReviewRequest request, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ReviewException("Review not found or does not belong to user"));

        if (review.getStatus() == ReviewStatus.BANNED) {
            throw new ReviewException("Banned review cannot be updated");
        }

        Long restaurantId = review.getRestaurant().getId();
        ReviewStatus status = spamService.moderateReview(request, userId, restaurantId);

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(status);

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toDto(updatedReview);
    }

    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ReviewException("Review not found or does not belong to user"));

        if (review.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new ReviewException("Review cannot be deleted");
        }

        reviewRepository.delete(review);
    }

    public List<ReviewResponse> getReviewsByRestaurant(Long restaurantId) {
        List<Review> reviews = reviewRepository.findAllByRestaurantIdAndStatus(restaurantId, ReviewStatus.APPROVED);
        return reviews.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
}
