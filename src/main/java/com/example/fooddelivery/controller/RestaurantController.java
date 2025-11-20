package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.enums.CuisineType;
import com.example.fooddelivery.service.ItemService;
import com.example.fooddelivery.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant Service")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@RequestBody @Valid RestaurantRequest restaurantRequest){
        RestaurantResponse response = restaurantService.createRestaurant(restaurantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getRestaurants(@RequestParam(required = false) CuisineType cuisine,
                                                                   @RequestParam(required = false) Double minRating){
        return ResponseEntity.ok(restaurantService.getRestaurants(cuisine, minRating));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id){
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable Long id,
                                                               @RequestBody @Valid RestaurantRequest restaurantRequest){
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restaurantRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeRestaurant(@PathVariable Long id){
        restaurantService.closeRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<ItemResponse> addItem(@PathVariable Long id,
                                                @RequestBody @Valid ItemRequest itemRequest){
        ItemResponse response = itemService.addItem(id, itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<ItemResponse>> getItems(@PathVariable Long id){
        return ResponseEntity.ok(itemService.getItems(id));
    }
}
