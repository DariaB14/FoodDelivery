package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.UserRequest;
import com.example.fooddelivery.dto.response.UserResponse;
import com.example.fooddelivery.enums.UserRole;
import com.example.fooddelivery.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Service")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest userRequest){
        UserResponse response = userService.register(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(summary = "Поиск пользователя по id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Обновление профиля пользователя")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @RequestBody @Valid UserRequest userRequest){
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    @Operation(summary = "Поиск пользователей по роли")
    @GetMapping
    public ResponseEntity<List<UserResponse>> findByRole(@RequestParam UserRole role){
        return ResponseEntity.ok(userService.findByRole(role));
    }

    @Operation(summary = "Деактивация пользователя")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id){
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
