package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long id,
                                                   @RequestBody @Valid ItemRequest itemRequest){
        return ResponseEntity.ok(itemService.updateItem(id, itemRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id){
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ItemResponse> updateAvailability(@PathVariable Long id,
                                                           @RequestParam boolean available){
        return ResponseEntity.ok(itemService.updateAvailability(id, available));
    }
}
