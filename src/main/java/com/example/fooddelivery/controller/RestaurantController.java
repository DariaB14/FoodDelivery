package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.enums.CuisineType;
import com.example.fooddelivery.service.ItemService;
import com.example.fooddelivery.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant Service")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final ItemService itemService;

    @Operation(summary = "Открыть ресторан")
    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@RequestBody @Valid RestaurantRequest restaurantRequest){
        RestaurantResponse response = restaurantService.createRestaurant(restaurantRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Найти все рестораны по кухни и рейтингу")
    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getRestaurants(@RequestParam(required = false) CuisineType cuisine,
                                                                   @RequestParam(required = false) Double minRating){
        return ResponseEntity.ok(restaurantService.getRestaurants(cuisine, minRating));
    }

    @Operation(summary = "Найти ресторан по id")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id){
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @Operation(summary = "Обновить ресторан")
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable Long id,
                                                               @RequestBody @Valid RestaurantRequest restaurantRequest){
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restaurantRequest));
    }

    @Operation(summary = "Закрыть ресторан")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeRestaurant(@PathVariable Long id){
        restaurantService.closeRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить блюдо в меню")
    @PostMapping("/{id}/menu")
    public ResponseEntity<ItemResponse> addItem(@PathVariable Long id,
                                                @RequestBody @Valid ItemRequest itemRequest){
        ItemResponse response = itemService.addItem(id, itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить все блюда ресторана по id")
    @GetMapping("/{id}/menu")
    public ResponseEntity<List<ItemResponse>> getItems(@PathVariable Long id){
        return ResponseEntity.ok(itemService.getItems(id));
    }
}
