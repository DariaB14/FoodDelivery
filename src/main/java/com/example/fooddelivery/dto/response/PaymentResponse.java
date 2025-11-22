package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.PaymentStatus;
import com.example.fooddelivery.enums.PaymentType;

public record PaymentResponse(Long id,
                              Long orderId,
                              PaymentStatus paymentStatus,
                              PaymentType paymentType
) {}
