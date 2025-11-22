package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.ReviewRequest;
import com.example.fooddelivery.dto.response.ReviewResponse;
import com.example.fooddelivery.enums.ReviewStatus;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.exception.ReviewException;
import com.example.fooddelivery.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    private final String BASE_URL = "/reviews";

    @BeforeEach
    void setUp(){
        reviewRequest = new ReviewRequest(1L, new BigDecimal("4.8"), "Очень вкусно и недорого. Спасибо!");
        reviewResponse = new ReviewResponse(1L, 1L, 5L, 1L, new BigDecimal("4.8"), "Очень вкусно и недорого. Спасибо!", ReviewStatus.APPROVED);
    }

    @Test
    void createReview_Success() throws Exception {
        when(reviewService.createReview(any(ReviewRequest.class), anyLong())).thenReturn(reviewResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("invalidReviewData")
    void createReviewWithInvalidData(String testName, ReviewRequest invalidRequest) throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidReviewData() {
        return Stream.of(
                Arguments.of("nullOrderId",
                        new ReviewRequest(null, new BigDecimal("4.7"), "Супер, все вкусно")),
                Arguments.of("nullRating",
                        new ReviewRequest(1L, null, "Супер, все вкусно")),
                Arguments.of("invalidRating",
                        new ReviewRequest(1L, new BigDecimal("10.0"), "Супер, все вкусно")),
                Arguments.of("emptyComment",
                        new ReviewRequest(1L, new BigDecimal("4.5"), "")),
                Arguments.of("nullComment",
                        new ReviewRequest(1L, new BigDecimal("4.5"), null)));
    }

    @Test
    void createReviewWhenOrderNotFound() throws Exception {
        when(reviewService.createReview(any(ReviewRequest.class), anyLong())).thenThrow(new EntityNotFoundException("Order with id 100 not found"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReview_WhenReviewException() throws Exception {
        when(reviewService.createReview(any(ReviewRequest.class), anyLong())).thenThrow(new ReviewException("Review for this order already exists"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviewsByRestaurant_Success() throws Exception {
        Long restaurantId = 5L;
        List<ReviewResponse> reviews = List.of(reviewResponse,
                new ReviewResponse(2L, 2L, restaurantId, 2L, new BigDecimal("4.5"), "Cпасибо за вкусный завтрак", ReviewStatus.APPROVED));

        when(reviewService.getReviewsByRestaurant(restaurantId)).thenReturn(reviews);

        mockMvc.perform(get(BASE_URL)
                        .param("restaurantId", restaurantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getReviewsByRestaurantWithoutParam() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviewsByRestaurantWhenReviewsNotFound() throws Exception {
        Long restaurantId = 100L;
        when(reviewService.getReviewsByRestaurant(restaurantId)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL)
                        .param("restaurantId", restaurantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateReview_Success() throws Exception {
        Long reviewId = 1L;
        ReviewRequest updateRequest = new ReviewRequest(1L, new BigDecimal("5.0"), "Великолепно, все вкусно и быстро!");
        ReviewResponse updatedResponse = new ReviewResponse(reviewId, 1L, 5L, 1L,
                new BigDecimal("5.0"), "Великолепно, все вкусно и быстро!", ReviewStatus.APPROVED);

        when(reviewService.updateReview(eq(reviewId), any(ReviewRequest.class), anyLong()))
                .thenReturn(updatedResponse);

        mockMvc.perform(put(BASE_URL + "/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateReviewWhenReviewNotFound() throws Exception {
        Long reviewId = 100L;
        when(reviewService.updateReview(eq(reviewId), any(ReviewRequest.class), anyLong()))
                .thenThrow(new ReviewException("Review not found or does not belong to user"));

        mockMvc.perform(put(BASE_URL + "/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReviewWhenReviewBanned() throws Exception {
        Long reviewId = 1L;
        when(reviewService.updateReview(eq(reviewId), any(ReviewRequest.class), anyLong())).thenThrow(new ReviewException("Banned review cannot be updated"));

        mockMvc.perform(put(BASE_URL + "/{id}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReview_Success() throws Exception {
        Long reviewId = 1L;
        doNothing().when(reviewService).deleteReview(eq(reviewId), anyLong());

        mockMvc.perform(delete(BASE_URL + "/{id}", reviewId))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(eq(reviewId), anyLong());
    }

    @Test
    void deleteReviewWhenReviewNotFound() throws Exception {
        Long reviewId = 100L;
        doThrow(new ReviewException("Review not found or does not belong to user")).when(reviewService).deleteReview(eq(reviewId), anyLong());

        mockMvc.perform(delete(BASE_URL + "/{id}", reviewId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReviewAfterOneDay() throws Exception {
        Long reviewId = 1L;
        doThrow(new ReviewException("Review cannot be deleted")).when(reviewService).deleteReview(eq(reviewId), anyLong());

        mockMvc.perform(delete(BASE_URL + "/{id}", reviewId))
                .andExpect(status().isBadRequest());
    }
}