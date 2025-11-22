package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.dto.request.UserRequest;
import com.example.fooddelivery.dto.response.UserResponse;
import com.example.fooddelivery.enums.UserRole;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRequest userRequest;
    private UserResponse userResponse;

    private static final String BASE_URL = "/users";
    private static final AddressDto ADDRESS = new AddressDto("Russia", "Kaliningrad", "Lenina", "5b", "111", 2);

    @BeforeEach
    void setUp(){
        userRequest = new UserRequest("Kate", "kate@gmail.com", "qwerty12", "+79991116677", ADDRESS, UserRole.CUSTOMER);
        userResponse = new UserResponse(1L, "Kate", "kate@gmail.com", "+79991116677", ADDRESS, UserRole.CUSTOMER, true);
    }

    @Test
    void registerWithValidData_Success() throws Exception{
        when(userService.register(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("invalidUserData")
    void registerWithInvalidData(String testName, UserRequest invalidUserRequest) throws Exception{
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());

    }

    private static Stream<Arguments> invalidUserData(){
        return Stream.of(
                Arguments.of("emptyUsername",
                        new UserRequest("", "kate@gmail.com", "qwerty12", "+79991116677", ADDRESS, UserRole.CUSTOMER)),
                Arguments.of("invalidEmail",
                        new UserRequest("Kate", "kate653", "qwerty12", "+79991116677", ADDRESS, UserRole.CUSTOMER)),
                Arguments.of("invalidPassword",
                        new UserRequest("Kate", "kate@gmail.com", "1234", "+79991116677", ADDRESS, UserRole.CUSTOMER)),
                Arguments.of("invalidPhone",
                        new UserRequest("Kate", "kate@gmail.com", "qwerty12", "799911167", ADDRESS, UserRole.CUSTOMER)),
                Arguments.of("invalidAddress",
                        new UserRequest("Kate", "kate@gmail.com", "qwerty12", "+79991116677",
                                new AddressDto("Russia", "", "Lenina", "5b", null, null), UserRole.CUSTOMER)),
                Arguments.of("nullRole",
                        new UserRequest("Kate", "kate@gmail.com", "qwerty12", "+79991116677", ADDRESS, null)));
    }

    @Test
    void findById_Success() throws Exception{
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(userResponse);

        mockMvc.perform(get(BASE_URL+"/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void findById_WhenUserNotFound() throws Exception{
        Long userId=100L;
        when(userService.findById(userId)).thenThrow(new EntityNotFoundException(String.format("User with id %d not found", userId)));

        mockMvc.perform(get(BASE_URL+"/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWithValidData_Success() throws Exception {
        Long userId = 1L;
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("Kate Updated");
        updateRequest.setEmail("kate_new@gmail.com");
        updateRequest.setPhone("+71112223344");
        updateRequest.setAddress(ADDRESS);
        updateRequest.setRole(UserRole.CUSTOMER);

        UserResponse updatedResponse = new UserResponse(userId, "Kate", "kate_new@gmail.com", "+71112223344", ADDRESS, UserRole.CUSTOMER, true);

        when(userService.update(eq(userId), any(UserRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put(BASE_URL + "/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("invalidUserData")
    void updateWithInvalidData(String testName, UserRequest invalidUserRequest) throws Exception {
        Long userId = 1L;

        mockMvc.perform(put(BASE_URL + "/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByRole_Success() throws Exception {
        UserRole role = UserRole.ADMIN;
        List<UserResponse> users = List.of(
                new UserResponse(2L, "admin1", "admin1@gmail.com", "+79996668544", ADDRESS, UserRole.ADMIN, true),
                new UserResponse(3L, "admin2", "admin2@gmail.com", "+79996468544", ADDRESS, UserRole.ADMIN, true)
        );

        when(userService.findByRole(role)).thenReturn(users);

        mockMvc.perform(get(BASE_URL)
                        .param("role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void findByRoleWithoutParam() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivate_Success() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deactivate(userId);

        mockMvc.perform(delete(BASE_URL + "/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deactivate(userId);
    }

    @Test
    void deactivateWhenUserNotFound() throws Exception {
        Long userId = 100L;
        doThrow(new EntityNotFoundException(String.format("User with id %d not found", userId)))
                .when(userService).deactivate(userId);

        mockMvc.perform(delete(BASE_URL + "/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
