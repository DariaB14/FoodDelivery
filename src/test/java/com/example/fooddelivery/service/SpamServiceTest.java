package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.ReviewRequest;
import com.example.fooddelivery.entity.Review;
import com.example.fooddelivery.enums.ReviewStatus;
import com.example.fooddelivery.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpamServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private SpamService spamService;

    private final Long USER_ID = 1L;
    private final Long RESTAURANT_ID = 5L;

    @Test
    void moderateReviewWithoutSpam() {
        ReviewRequest request = new ReviewRequest(1L, new BigDecimal("4.5"), "Отличная еда и быстрая доставка!");

        when(reviewRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
        when(reviewRepository.findLast50ReviewsByRestaurantId(RESTAURANT_ID)).thenReturn(List.of());

        ReviewStatus result = spamService.moderateReview(request, USER_ID, RESTAURANT_ID);

        assertThat(result).isEqualTo(ReviewStatus.APPROVED);
    }

    @Test
    void moderateReviewWithStopWords() {
        ReviewRequest request = new ReviewRequest(1L, new BigDecimal("4.5"), "Хорошая работа, звоните");

        ReviewStatus result = spamService.moderateReview(request, USER_ID, RESTAURANT_ID);

        assertThat(result).isEqualTo(ReviewStatus.BANNED);
    }

    @Test
    void moderateReviewWithCopyPasteFromUserHistory() {
        String comment = "Все отлично. Приду еще";
        ReviewRequest request = new ReviewRequest(1L, new BigDecimal("4.5"), comment);
        Review existingReview = createReview(comment);

        when(reviewRepository.findAllByUserId(USER_ID)).thenReturn(List.of(existingReview));

        ReviewStatus result = spamService.moderateReview(request, USER_ID, RESTAURANT_ID);

        assertThat(result).isEqualTo(ReviewStatus.BANNED);

    }

    @Test
    void moderateReviewWithCopyPasteFromRestaurantReviews() {
        String comment = "Все отлично. Приду еще";
        ReviewRequest request = new ReviewRequest(1L, new BigDecimal("4.5"), comment);
        Review existingReview = createReview(comment);

        when(reviewRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
        when(reviewRepository.findLast50ReviewsByRestaurantId(RESTAURANT_ID)).thenReturn(List.of(existingReview));

        ReviewStatus result = spamService.moderateReview(request, USER_ID, RESTAURANT_ID);

        assertThat(result).isEqualTo(ReviewStatus.BANNED);
    }

    private Review createReview(String comment) {
        Review review = new Review();
        review.setComment(comment);
        return review;
    }

}