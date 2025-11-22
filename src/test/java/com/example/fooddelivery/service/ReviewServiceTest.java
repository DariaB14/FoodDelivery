package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.ReviewMapper;
import com.example.fooddelivery.dto.request.ReviewRequest;
import com.example.fooddelivery.dto.response.ReviewResponse;
import com.example.fooddelivery.entity.*;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.enums.ReviewStatus;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.exception.ReviewException;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private SpamService spamService;

    @InjectMocks
    private ReviewService reviewService;

    private final Long USER_ID = 1L;
    private final Long OTHER_USER_ID = 2L;
    private final Long ORDER_ID = 1L;
    private final Long REVIEW_ID = 1L;
    private final Long RESTAURANT_ID = 4L;

    private static final Address ADDRESS = new Address("Russia", "Kaliningrad", "Lenina", "5b");

    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp(){
        reviewRequest = new ReviewRequest(ORDER_ID, new BigDecimal("4.5"), "Очень вкусно и быстро");
    }

    @Test
    void createReview_Success() {
        Order order = createOrder(USER_ID, OrderStatus.DELIVERED, RESTAURANT_ID);
        Review review = createReview(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);
        ReviewResponse response = createReviewResponse(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);

        when(reviewRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(spamService.moderateReview(reviewRequest, USER_ID, RESTAURANT_ID)).thenReturn(ReviewStatus.APPROVED);
        when(reviewMapper.toEntity(reviewRequest)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toDto(review)).thenReturn(response);

        ReviewResponse result = reviewService.createReview(reviewRequest, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(REVIEW_ID);
        assertThat(review.getUser().getId()).isEqualTo(USER_ID);
        assertThat(review.getRestaurant().getId()).isEqualTo(RESTAURANT_ID);
        assertThat(review.getOrder().getId()).isEqualTo(ORDER_ID);

        verify(reviewRepository).save(review);
    }

    @Test
    void createReviewWhenReviewExists() {
        when(reviewRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

        assertThrows(ReviewException.class, () -> reviewService.createReview(reviewRequest, USER_ID));

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest, USER_ID))
                .isInstanceOf(ReviewException.class)
                .hasMessage("Review for this order already exists");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewWhenOrderNotFound() {
        when(reviewRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest, USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                        .hasMessage("Order with id %d not found", ORDER_ID);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewWhenOrderBelongsToOtherUser() {
        Order order = createOrder(OTHER_USER_ID, OrderStatus.DELIVERED, RESTAURANT_ID);

        when(reviewRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest, USER_ID))
                .isInstanceOf(ReviewException.class)
                .hasMessage("This order belongs to other user");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewWhenOrderNotDelivered() {
        Order order = createOrder(USER_ID, OrderStatus.CONFIRMED, RESTAURANT_ID);

        when(reviewRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest, USER_ID))
                .isInstanceOf(ReviewException.class)
                .hasMessage("Review can be left only for delivered orders");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewWithSpam() {
        ReviewRequest request = new ReviewRequest(ORDER_ID, new BigDecimal("4.5"), "Работа и заработок");
        Order order = createOrder(USER_ID, OrderStatus.DELIVERED, RESTAURANT_ID);
        Review review = createReview(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);
        ReviewResponse response = new ReviewResponse(REVIEW_ID, USER_ID, RESTAURANT_ID, ORDER_ID, new BigDecimal("4.5"), "Работа и заработок", ReviewStatus.BANNED);

        when(reviewRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(spamService.moderateReview(request, USER_ID, RESTAURANT_ID)).thenReturn(ReviewStatus.BANNED);
        when(reviewMapper.toEntity(request)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toDto(review)).thenReturn(response);

        ReviewResponse result = reviewService.createReview(request, USER_ID);

        assertThat(result.status()).isEqualTo(ReviewStatus.BANNED);
        assertThat(review.getStatus()).isEqualTo(ReviewStatus.BANNED);

        verify(spamService).moderateReview(request, USER_ID, RESTAURANT_ID);
    }

    @Test
    void updateReview_Success() {
        Review review = createReview(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);

        ReviewResponse response = new ReviewResponse(REVIEW_ID, USER_ID, RESTAURANT_ID, ORDER_ID,
                new BigDecimal("5.0"), "Великолепно!", ReviewStatus.APPROVED);

        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.of(review));
        when(spamService.moderateReview(reviewRequest, USER_ID, RESTAURANT_ID)).thenReturn(ReviewStatus.APPROVED);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toDto(review)).thenReturn(response);

        ReviewResponse result = reviewService.updateReview(REVIEW_ID, reviewRequest, USER_ID);

        assertThat(result.rating()).isEqualTo(new BigDecimal("5.0"));

        verify(reviewRepository).save(review);
    }

    @Test
    void updateReviewWhenReviewNotFound() {
        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, reviewRequest, USER_ID))
                .isInstanceOf(ReviewException.class)
                .hasMessage("Review not found or does not belong to user");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReviewWhenReviewBanned() {
        Review review = createReview(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.BANNED);

        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.updateReview(REVIEW_ID, reviewRequest, USER_ID))
                .isInstanceOf(ReviewException.class)
                .hasMessage("Banned review cannot be updated");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_Success() {
        Review review = createReview(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);

        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.of(review));

        reviewService.deleteReview(REVIEW_ID, USER_ID);

        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_WhenReviewNotFound_ShouldThrowException() {
        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, USER_ID))
                .isInstanceOf(ReviewException.class)
                .hasMessage("Review not found or does not belong to user");

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReviewAfter24Hours() {
        Review review = createReview(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);
        review.setCreatedAt(LocalDateTime.now().minusHours(25));

        when(reviewRepository.findByIdAndUserId(REVIEW_ID, USER_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, USER_ID))
                .isInstanceOf(ReviewException.class)
                .hasMessage("Review cannot be deleted");

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void getReviewsByRestaurant_Success() {
        Review review = createReview(2L, OTHER_USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);

        ReviewResponse response = createReviewResponse(REVIEW_ID, USER_ID, RESTAURANT_ID, ReviewStatus.APPROVED);

        when(reviewRepository.findAllByRestaurantIdAndStatus(RESTAURANT_ID, ReviewStatus.APPROVED))
                .thenReturn(List.of(review));
        when(reviewMapper.toDto(review)).thenReturn(response);

        List<ReviewResponse> result = reviewService.getReviewsByRestaurant(RESTAURANT_ID);

        assertThat(result).hasSize(1);

        verify(reviewRepository).findAllByRestaurantIdAndStatus(RESTAURANT_ID, ReviewStatus.APPROVED);
    }

    @Test
    void getReviewsByRestaurantWhenNoReviews() {
        when(reviewRepository.findAllByRestaurantIdAndStatus(RESTAURANT_ID, ReviewStatus.APPROVED))
                .thenReturn(List.of());

        List<ReviewResponse> result = reviewService.getReviewsByRestaurant(RESTAURANT_ID);

        assertThat(result).isEmpty();

        verify(reviewRepository).findAllByRestaurantIdAndStatus(RESTAURANT_ID, ReviewStatus.APPROVED);
    }


    private Order createOrder(Long userId, OrderStatus status, Long restaurantId) {
        User user = new User();
        user.setId(userId);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Cart cart = new Cart();
        cart.setRestaurant(restaurant);

        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUser(user);
        order.setCart(cart);
        order.setStatus(status);

        return order;
    }

    private Review createReview(Long reviewId, Long userId, Long restaurantId, ReviewStatus status) {
        User user = new User();
        user.setId(userId);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        Order order = createOrder(userId, OrderStatus.DELIVERED, restaurantId);

        Review review = new Review();
        review.setId(reviewId);
        review.setUser(user);
        review.setRestaurant(restaurant);
        review.setOrder(order);
        review.setRating(new BigDecimal("4.5"));
        review.setComment("Вкусно и недорого. Рекомендую");
        review.setStatus(status);
        review.setCreatedAt(LocalDateTime.now());

        return review;
    }

    private ReviewResponse createReviewResponse(Long reviewId, Long userId, Long restaurantId, ReviewStatus status) {
        return new ReviewResponse(reviewId, userId, restaurantId, ORDER_ID,
                new BigDecimal("4.5"), "Вкусно и недорого. Рекомендую!", status);
    }
}