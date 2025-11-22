package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.dto.mapper.RestaurantMapper;
import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.enums.CuisineType;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantMapper restaurantMapper;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private final Long RESTAURANT_ID = 1L;
    private final Long NON_EXISTENT_RESTAURANT_ID = 999L;

    private RestaurantRequest restaurantRequest;
    private RestaurantResponse restaurantResponse;
    private Restaurant restaurant;

    private static final Address ADDRESS_ENTITY = new Address("Russia", "Kaliningrad", "Lenina", "5b");
    private static final AddressDto ADDRESS = new AddressDto("Russia", "Kaliningrad", "Lenina", "5b", "111", 2);
    private static final BigDecimal RATING = new BigDecimal("4.3");
    private static final LocalTime OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);

    @BeforeEach
    void setUp() {
        restaurantRequest = new RestaurantRequest("PizzaMania", ADDRESS, CuisineType.PIZZA, OPEN_TIME, CLOSE_TIME);

        restaurantResponse = new RestaurantResponse(RESTAURANT_ID, "PizzaMania", ADDRESS, CuisineType.PIZZA, RATING, OPEN_TIME, CLOSE_TIME, true);

        restaurant = new Restaurant();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setName("PizzaMania");
        restaurant.setAddress(ADDRESS_ENTITY);
        restaurant.setCuisineType(CuisineType.PIZZA);
        restaurant.setRating(RATING);
        restaurant.setOpeningTime(OPEN_TIME);
        restaurant.setClosingTime(CLOSE_TIME);
        restaurant.setActive(true);
    }

    @Test
    void createRestaurant_Success() {
        Restaurant newRestaurant = new Restaurant();
        newRestaurant.setName("PizzaHouse");
        newRestaurant.setAddress(ADDRESS_ENTITY);
        newRestaurant.setCuisineType(CuisineType.PIZZA);
        newRestaurant.setOpeningTime(OPEN_TIME);
        newRestaurant.setClosingTime(CLOSE_TIME);

        when(restaurantMapper.toEntity(restaurantRequest)).thenReturn(newRestaurant);
        when(restaurantRepository.save(newRestaurant)).thenReturn(restaurant);
        when(restaurantMapper.toDto(restaurant)).thenReturn(restaurantResponse);

        RestaurantResponse result = restaurantService.createRestaurant(restaurantRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(RESTAURANT_ID);
        assertThat(result.name()).isEqualTo("PizzaMania");

        verify(restaurantRepository).save(newRestaurant);
    }

    @Test
    void getRestaurantById_Success() {
        BigDecimal currentRating = new BigDecimal("4.5");

        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.calculateAverageRatingByRestaurantId(RESTAURANT_ID)).thenReturn(currentRating);
        when(restaurantMapper.toDto(restaurant)).thenReturn(restaurantResponse);

        RestaurantResponse result = restaurantService.getRestaurantById(RESTAURANT_ID);

        assertThat(result).isNotNull();

        verify(reviewRepository).calculateAverageRatingByRestaurantId(RESTAURANT_ID);
        verify(restaurantMapper).toDto(restaurant);
    }

    @Test
    void getRestaurantByIdWhenRestaurantNotFound() {
        when(restaurantRepository.findById(NON_EXISTENT_RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getRestaurantById(NON_EXISTENT_RESTAURANT_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Restaurant with id 999 not found");
    }

    @Test
    void getRestaurantsWithCuisineFilter() {
        CuisineType cuisine = CuisineType.PIZZA;
        List<Restaurant> restaurants = List.of(restaurant);
        BigDecimal rating1 = new BigDecimal("4.3");

        when(restaurantRepository.findByCuisineAndRating(cuisine, null)).thenReturn(restaurants);
        when(reviewRepository.calculateAverageRatingByRestaurantId(RESTAURANT_ID)).thenReturn(rating1);
        when(restaurantMapper.toDto(restaurant)).thenReturn(restaurantResponse);

        List<RestaurantResponse> result = restaurantService.getRestaurants(cuisine, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).cuisineType()).isEqualTo(CuisineType.PIZZA);

        verify(restaurantRepository).findByCuisineAndRating(cuisine, null);
    }

    @Test
    void getRestaurantsWithMinRatingFilter() {
        Double minRating = 4.0;

        when(restaurantRepository.findByCuisineAndRating(null, minRating)).thenReturn(List.of(restaurant));
        when(reviewRepository.calculateAverageRatingByRestaurantId(RESTAURANT_ID)).thenReturn(RATING);
        when(restaurantMapper.toDto(restaurant)).thenReturn(restaurantResponse);

        List<RestaurantResponse> result = restaurantService.getRestaurants(null, minRating);

        assertThat(result).hasSize(1);
    }

    @Test
    void getRestaurantsWithoutFilters() {
        when(restaurantRepository.findByCuisineAndRating(null, null)).thenReturn(List.of(restaurant));
        when(reviewRepository.calculateAverageRatingByRestaurantId(RESTAURANT_ID)).thenReturn(RATING);
        when(restaurantMapper.toDto(restaurant)).thenReturn(restaurantResponse);

        List<RestaurantResponse> result = restaurantService.getRestaurants(null, null);

        assertThat(result).hasSize(1);

        verify(restaurantRepository).findByCuisineAndRating(null, null);
    }

    @Test
    void updateRestaurant_Success() {
        RestaurantRequest updateRequest = new RestaurantRequest("PizzaMania New", ADDRESS, CuisineType.ITALIAN, LocalTime.of(9, 0), LocalTime.of(23, 0));

        Restaurant updatedRestaurant = new Restaurant();
        updatedRestaurant.setId(RESTAURANT_ID);
        updatedRestaurant.setName("PizzaMania New");

        RestaurantResponse updatedResponse = new RestaurantResponse(RESTAURANT_ID, "PizzaMania New", ADDRESS, CuisineType.ITALIAN, RATING,
                LocalTime.of(9, 0), LocalTime.of(23, 0), true);

        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(updatedRestaurant);
        when(restaurantMapper.toDto(updatedRestaurant)).thenReturn(updatedResponse);

        RestaurantResponse result = restaurantService.updateRestaurant(RESTAURANT_ID, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("PizzaMania New");
        assertThat(result.cuisineType()).isEqualTo(CuisineType.ITALIAN);

        verify(restaurantMapper).update(updateRequest, restaurant);
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void updateRestaurantWhenRestaurantNotFound() {
        when(restaurantRepository.findById(NON_EXISTENT_RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.updateRestaurant(NON_EXISTENT_RESTAURANT_ID, restaurantRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Restaurant with id 999 not found");

        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void closeRestaurant_Success() {
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

        restaurantService.closeRestaurant(RESTAURANT_ID);

        assertThat(restaurant.isActive()).isFalse();

        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void closeRestaurantWhenRestaurantNotFound() {
        when(restaurantRepository.findById(NON_EXISTENT_RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.closeRestaurant(NON_EXISTENT_RESTAURANT_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Restaurant with id 999 not found");

        verify(restaurantRepository, never()).save(any());
    }
}