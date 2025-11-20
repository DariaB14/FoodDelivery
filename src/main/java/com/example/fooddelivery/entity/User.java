package com.example.fooddelivery.entity;

import com.example.fooddelivery.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String username;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Size(min = 6)
    @Column(nullable = false)
    private String password;

    @Pattern(regexp = "^\\+7\\d{10}$", message = "The phone number format should be: +7xxxxxxxxxx ")
    @Column
    private String phone;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(String username, String email, String password, String phone, UserRole role, boolean active) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.active=active;
    }
}
