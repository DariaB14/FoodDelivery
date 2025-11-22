package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.RestaurantMapper;
import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.enums.CuisineType;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final ReviewRepository reviewRepository;

    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = restaurantMapper.toEntity(request);
        restaurant.setRating(BigDecimal.ZERO);
        restaurant.setActive(true);
        Restaurant saved = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurants(CuisineType cuisine, Double minRating) {
        List<Restaurant> restaurants = restaurantRepository.findByCuisineAndRating(cuisine, minRating);

        return restaurants.stream()
                .map(restaurant -> {
                    BigDecimal currentRating = reviewRepository.calculateAverageRatingByRestaurantId(restaurant.getId());
                    restaurant.setRating(currentRating);
                    return restaurantMapper.toDto(restaurant);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Restaurant with id %d not found", id)));

        BigDecimal curRating = reviewRepository.calculateAverageRatingByRestaurantId(id);
        restaurant.setRating(curRating);
        return restaurantMapper.toDto(restaurant);
    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Restaurant with id %d not found", id)));

        restaurantMapper.update(request, restaurant);
        Restaurant updated = restaurantRepository.save(restaurant);
        return restaurantMapper.toDto(updated);
    }

    public void closeRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Restaurant with id %d not found", id)));
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }
}
