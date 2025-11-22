package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.ReviewRequest;
import com.example.fooddelivery.dto.response.ReviewResponse;
import com.example.fooddelivery.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Service")
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "Создать отзыв ресторану")
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request){
        Long userId = getCurrentUser();
        ReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить все отзвы о ресторане")
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviewsByRestaurant(@RequestParam Long restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsByRestaurant(restaurantId));
    }

    @Operation(summary = "Обновить отзыв")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id,
                                                       @Valid @RequestBody ReviewRequest request) {

        Long userId = getCurrentUser();
        return ResponseEntity.ok(reviewService.updateReview(id, request, userId));
    }

    @Operation(summary = "Удалить отзыв")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Long userId = getCurrentUser();
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }

    //заглушка для security
    private Long getCurrentUser(){
        return 1L;
    }
}
