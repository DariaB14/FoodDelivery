package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.ReviewRequest;
import com.example.fooddelivery.entity.Review;
import com.example.fooddelivery.enums.ReviewStatus;
import com.example.fooddelivery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpamService {
    private final ReviewRepository reviewRepository;

    private final Set<String> STOP_WORDS = Set.of("работа", "заработок", "казино", "ставки", "http", "https", "купите", "вступите", "tg", "bot");

    public ReviewStatus moderateReview(ReviewRequest request, Long userId, Long restaurantId){
        String message = request.getComment();

        if(containsStopWords(message)){
            return ReviewStatus.BANNED;
        }
        if(isCopyPasteFromUserReviewHistory(message, userId)){
            return ReviewStatus.BANNED;
        }
        if(isCopyPasteFromRestaurantReviews(message, restaurantId)){
            return ReviewStatus.BANNED;
        }
        return ReviewStatus.APPROVED;
    }

    private boolean containsStopWords(String message){
        String msg = message.toLowerCase();
        return STOP_WORDS.stream()
                .anyMatch(msg::contains);
    }

    private boolean isCopyPasteFromUserReviewHistory(String message, Long userId){
        List<Review> userReviews = reviewRepository.findAllByUserId(userId);
        return userReviews.stream()
                .map(Review::getComment)
                .anyMatch(comment -> comment.equalsIgnoreCase(message));
    }

    private boolean isCopyPasteFromRestaurantReviews(String message, Long restaurantId) {
        List<Review> lastRestaurantReviews = reviewRepository.findLast50ReviewsByRestaurantId(restaurantId);

        return lastRestaurantReviews.stream()
                .map(Review::getComment)
                .anyMatch(comment -> comment.equalsIgnoreCase(message));
    }
}
