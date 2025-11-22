package com.example.fooddelivery.entity;

import com.example.fooddelivery.enums.CourierStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@NoArgsConstructor
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Pattern(regexp = "^\\+7\\d{10}$", message = "The phone number format should be: +7xxxxxxxxxx ")
    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourierStatus status = CourierStatus.OFFLINE;

    @Column(name = "rating", precision = 3, scale = 2)
    @Min(0)
    @Max(5)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "current_orders_amount")
    private Integer currentOrdersAmount = 0;

    @OneToMany(mappedBy = "courier")
    private List<Order> orders = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Courier(String name, String phone, CourierStatus status, BigDecimal rating, Integer currentOrdersAmount) {
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.rating = rating;
        this.currentOrdersAmount = currentOrdersAmount;
    }
}
