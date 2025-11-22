package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Tag(name = "Item Service")
public class ItemController {
    private final ItemService itemService;

    @Operation(summary = "Обновить позицию в меню")
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long id,
                                                   @RequestBody @Valid ItemRequest itemRequest){
        return ResponseEntity.ok(itemService.updateItem(id, itemRequest));
    }

    @Operation(summary = "Удалить позицию из меню")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id){
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Изменить доступность блюда")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ItemResponse> updateAvailability(@PathVariable Long id,
                                                           @RequestParam boolean available){
        return ResponseEntity.ok(itemService.updateAvailability(id, available));
    }
}
