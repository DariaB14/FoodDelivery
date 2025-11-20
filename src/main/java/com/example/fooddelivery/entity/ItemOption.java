package com.example.fooddelivery.entity;

import com.example.fooddelivery.enums.ItemSize;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item_options")
@Getter
@Setter
@NoArgsConstructor
public class ItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSize size;

    @DecimalMin("0.0")
    @Column(nullable = false)
    private BigDecimal price;

    @Column(name="preparation_minutes", nullable = false)
    private Integer preparationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @OneToMany(mappedBy = "itemOption", fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();
}
