package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.dto.mapper.UserMapper;
import com.example.fooddelivery.dto.request.UserRequest;
import com.example.fooddelivery.dto.response.UserResponse;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.UserRole;
import com.example.fooddelivery.exception.EmailException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private final Long USER_ID = 1L;
    private final Long NON_EXISTENT_USER_ID = 999L;

    private UserRequest userRequest;
    private User user;
    private UserResponse userResponse;

    private static final Address ADDRESS = new Address("Russia", "Kaliningrad", "Lenina", "5b");
    private static final AddressDto ADDRESS_DTO = new AddressDto("Russia", "Kaliningrad", "Lenina", "5b", "111", 2);

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest("Kate", "kate@gmail.com", "password123", "+79971112233", ADDRESS_DTO, UserRole.CUSTOMER);

        user = new User("Kate", "kate@gmail.com", "password123", "+79971112233", UserRole.CUSTOMER, true);
        user.setId(USER_ID);

        userResponse = new UserResponse(USER_ID, "Kate", "kate@gmail.com", "+79971112233", ADDRESS_DTO, UserRole.CUSTOMER, true);
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail("kate@gmail.com")).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.register(userRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);

        verify(userRepository).save(user);
    }

    @Test
    void registerWhenEmailExists() {
        when(userRepository.existsByEmail("kate@gmail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(userRequest))
                .isInstanceOf(EmailException.class)
                .hasMessage("Email is already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findById_Success() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.findById(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);

        verify(userRepository).findById(USER_ID);
    }

    @Test
    void findById_WhenUserNotFound() {
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(NON_EXISTENT_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id 999 not found");
    }

    @Test
    void findByIdWhenUserDeactivated() {
        user.setActive(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.findById(USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id 1 is deactivated");
    }

    @Test
    void update_Success() {
        UserRequest updateRequest = new UserRequest("Kate", "kate_new@gmail.com", "newpassword",
                "+79971114455", ADDRESS_DTO, UserRole.CUSTOMER);
        User updatedUser = new User("Kate", "kate_new@gmail.com", "newpassword", "+79971114455", UserRole.CUSTOMER, true);
        UserResponse updatedResponse = new UserResponse(USER_ID, "Kate", "kate_new@gmail.com", "+79971114455", ADDRESS_DTO, UserRole.CUSTOMER, true);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("kate_new@gmail.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedResponse);

        UserResponse result = userService.update(USER_ID, updateRequest);

        assertThat(result).isNotNull();


        verify(userMapper).update(updateRequest, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateWhenUserNotFound() {
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(NON_EXISTENT_USER_ID, userRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id 999 not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByRole_Success(){
        User user1 = new User("User1", "user1@gmail.com", "password1", "+79991110001", UserRole.CUSTOMER, true);
        User user2 = new User("User2", "user2@gmail.com", "password2", "+79991110002", UserRole.CUSTOMER, true);

        UserResponse response1 = new UserResponse(1L, "User1", "user1@gmail.com", "+79991110001", null, UserRole.CUSTOMER, true);
        UserResponse response2 = new UserResponse(2L, "User2", "user2@gmail.com", "+79991110002", null, UserRole.CUSTOMER, true);

        when(userRepository.findByRole(UserRole.CUSTOMER)).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(response1);
        when(userMapper.toDto(user2)).thenReturn(response2);

        List<UserResponse> result = userService.findByRole(UserRole.CUSTOMER);

        assertThat(result).hasSize(2);

        verify(userRepository).findByRole(UserRole.CUSTOMER);
    }

    @Test
    void deactivate_Success(){
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        userService.deactivate(USER_ID);

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void deactivate_WhenUserNotFound(){
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivate(NON_EXISTENT_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id 999 not found");

        verify(userRepository, never()).save(any());
    }
}