package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.dto.request.ItemOptionRequest;
import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.ItemOptionResponse;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.enums.CuisineType;
import com.example.fooddelivery.enums.ItemSize;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.service.ItemService;
import com.example.fooddelivery.service.RestaurantService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private ItemService itemService;

    private RestaurantRequest restaurantRequest;
    private RestaurantResponse restaurantResponse;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;
    private ItemOptionRequest itemOptionRequest;
    private ItemOptionResponse itemOptionResponse;

    private static final String BASE_URL = "/restaurants";

    private static final AddressDto ADDRESS = new AddressDto("Russia", "Kaliningrad", "Lenina", "5b", "111", 2);

    @BeforeEach
    void setUp(){
        restaurantRequest = new RestaurantRequest("PizzaMania", ADDRESS, CuisineType.PIZZA, LocalTime.of(10, 0), LocalTime.of(22, 0));
        restaurantResponse = new RestaurantResponse(1L, "PizzaMania", ADDRESS, CuisineType.PIZZA, new BigDecimal("4.3"), LocalTime.of(10, 0), LocalTime.of(22, 0), true);

        itemOptionRequest = new ItemOptionRequest(ItemSize.LARGE, new BigDecimal("1000.00"), 20);
        itemOptionResponse = new ItemOptionResponse(1L, ItemSize.LARGE, new BigDecimal("1000.00"), 20);

        itemRequest = new ItemRequest("Pepperoni", true, List.of(itemOptionRequest));
        itemResponse = new ItemResponse(1L, "Pepperoni", true, 1L, List.of(itemOptionResponse));
    }

    @Test
    void createRestaurant_Success() throws Exception{
        when(restaurantService.createRestaurant(any(RestaurantRequest.class))).thenReturn(restaurantResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurantRequest)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("invalidRestaurantData")
    void createRestaurantWithInvalidData(String testName, RestaurantRequest restaurantRequest) throws Exception{
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurantRequest)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidRestaurantData(){
        return Stream.of(
                Arguments.of("emptyName",
                        new RestaurantRequest("", ADDRESS, CuisineType.ITALIAN, LocalTime.of(9, 0), LocalTime.of(23, 0))),
                Arguments.of("nullName",
                        new RestaurantRequest(null, ADDRESS, CuisineType.ITALIAN, LocalTime.of(9, 0), LocalTime.of(23, 0))),
                Arguments.of("nullAddress",
                        new RestaurantRequest("Pizza House", null, CuisineType.ITALIAN, LocalTime.of(9, 0), LocalTime.of(23, 0))),
                Arguments.of("nullCuisineType",
                        new RestaurantRequest("Pizza House", ADDRESS, null, LocalTime.of(9, 0), LocalTime.of(23, 0))),
                Arguments.of("nullOpeningTime",
                        new RestaurantRequest("Pizza House", ADDRESS, CuisineType.ITALIAN, null, LocalTime.of(23, 0))),
                Arguments.of("nullClosingTime",
                        new RestaurantRequest("Pizza House", ADDRESS, CuisineType.ITALIAN, LocalTime.of(9, 0), null))
        );
    }

    @Test
    void getRestaurantsWithoutFilters() throws Exception {
        List<RestaurantResponse> restaurants = List.of(restaurantResponse);

        when(restaurantService.getRestaurants(null, null)).thenReturn(restaurants);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRestaurantsWithCuisineFilter() throws Exception {
        CuisineType cuisine = CuisineType.PIZZA;
        List<RestaurantResponse> restaurants = List.of(restaurantResponse);

        when(restaurantService.getRestaurants(cuisine, null)).thenReturn(restaurants);

        mockMvc.perform(get(BASE_URL)
                        .param("cuisine", cuisine.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRestaurantsWithRatingFilter() throws Exception {
        Double minRating = 4.0;
        List<RestaurantResponse> restaurants = List.of(restaurantResponse);

        when(restaurantService.getRestaurants(null, minRating)).thenReturn(restaurants);

        mockMvc.perform(get(BASE_URL)
                        .param("minRating", minRating.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRestaurantsWithCuisineAndRatingFilters() throws Exception {
        CuisineType cuisine = CuisineType.PIZZA;
        Double minRating = 4.0;
        List<RestaurantResponse> restaurants = List.of(restaurantResponse);

        when(restaurantService.getRestaurants(cuisine, minRating)).thenReturn(restaurants);

        mockMvc.perform(get(BASE_URL)
                        .param("cuisine", cuisine.toString())
                        .param("minRating", minRating.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRestaurantById_Success() throws Exception {
        Long restaurantId = 1L;
        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(restaurantResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", restaurantId))
                .andExpect(status().isOk());
    }

    @Test
    void getRestaurantByIdWhenRestaurantNotFound() throws Exception {
        Long restaurantId = 100L;

        when(restaurantService.getRestaurantById(restaurantId)).thenThrow(new EntityNotFoundException("Restaurant with id 100 not found"));

        mockMvc.perform(get(BASE_URL + "/{id}", restaurantId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRestaurant_Success() throws Exception {
        Long restaurantId = 1L;
        RestaurantRequest updateRequest = new RestaurantRequest(
                "Pizza House",
                ADDRESS,
                CuisineType.ITALIAN,
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
        );

        RestaurantResponse updatedResponse = new RestaurantResponse(restaurantId, "Pizza House", restaurantResponse.address(),
                CuisineType.ITALIAN, new BigDecimal("4.3"), LocalTime.of(10, 0), LocalTime.of(22, 0), true);

        when(restaurantService.updateRestaurant(eq(restaurantId), any(RestaurantRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put(BASE_URL + "/{id}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateRestaurantWhenRestaurantNotFound() throws Exception {
        Long restaurantId = 100L;
        when(restaurantService.updateRestaurant(eq(restaurantId), any(RestaurantRequest.class)))
                .thenThrow(new EntityNotFoundException("Restaurant with id 100 not found"));

        mockMvc.perform(put(BASE_URL + "/{id}", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurantRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void closeRestaurant_Success() throws Exception {
        Long restaurantId = 1L;
        doNothing().when(restaurantService).closeRestaurant(restaurantId);

        mockMvc.perform(delete(BASE_URL + "/{id}", restaurantId)).andExpect(status().isNoContent());

        verify(restaurantService).closeRestaurant(restaurantId);
    }

    @Test
    void closeRestaurantWhenRestaurantNotFound() throws Exception {
        Long restaurantId = 100L;
        doThrow(new EntityNotFoundException("Restaurant with id 100 not found")).when(restaurantService).closeRestaurant(restaurantId);

        mockMvc.perform(delete(BASE_URL + "/{id}", restaurantId))
                .andExpect(status().isNotFound());
    }

    @Test
    void addItem_Success() throws Exception {
        Long restaurantId = 1L;
        when(itemService.addItem(eq(restaurantId), any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(post(BASE_URL + "/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void addItemWhenRestaurantNotFound() throws Exception {
        Long restaurantId = 100L;
        when(itemService.addItem(eq(restaurantId), any(ItemRequest.class))).thenThrow(new EntityNotFoundException("Restaurant with id 100 not found"));

        mockMvc.perform(post(BASE_URL + "/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItems_Success() throws Exception {
        Long restaurantId = 1L;
        List<ItemResponse> items = List.of(itemResponse,
                new ItemResponse(2L, "Margarita", true, restaurantId,
                        List.of(new ItemOptionResponse(2L, ItemSize.LARGE, new BigDecimal("900.00"), 15))));

        when(itemService.getItems(restaurantId)).thenReturn(items);

        mockMvc.perform(get(BASE_URL + "/{id}/menu", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getItemsWhenRestaurantNotFound() throws Exception {
        Long restaurantId = 100L;
        when(itemService.getItems(restaurantId))
                .thenThrow(new EntityNotFoundException("Restaurant with id 100 not found"));

        mockMvc.perform(get(BASE_URL + "/{id}/menu", restaurantId))
                .andExpect(status().isNotFound());
    }
}