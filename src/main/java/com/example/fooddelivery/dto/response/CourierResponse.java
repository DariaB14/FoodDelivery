package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.CourierStatus;

import java.math.BigDecimal;

public record CourierResponse(Long id,
                              String name,
                              String phone,
                              CourierStatus status,
                              BigDecimal rating,
                              Integer currentOrdersAmount
) {}
