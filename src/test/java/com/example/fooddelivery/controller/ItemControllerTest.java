package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.ItemOptionRequest;
import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.response.ItemOptionResponse;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.enums.ItemSize;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.service.ItemService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemOptionRequest itemOptionRequest;
    private ItemOptionResponse itemOptionResponse;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;

    private static final String BASE_URL = "/menu";

    @BeforeEach
    void setUp() {
        itemOptionRequest = new ItemOptionRequest(ItemSize.LARGE, new BigDecimal("1000.00"), 20);
        itemRequest = new ItemRequest("Pepperoni", true, List.of(itemOptionRequest));

        itemOptionResponse = new ItemOptionResponse(1L, ItemSize.LARGE, new BigDecimal("1000.00"), 20);
        itemResponse = new ItemResponse(1L, "Pepperoni", true, 5L, List.of(itemOptionResponse));
    }

    @Test
    void updateItem_Success() throws Exception {
        Long itemId = 1L;
        when(itemService.updateItem(eq(itemId), any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(put(BASE_URL + "/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("invalidItemData")
    void updateItemWithInvalidData(String testName, ItemRequest invalidRequest) throws Exception {
        Long itemId = 1L;

        mockMvc.perform(put(BASE_URL + "/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidItemData() {
        return Stream.of(
                Arguments.of("emptyName",
                        new ItemRequest("", true, List.of())),
                Arguments.of("nullName",
                        new ItemRequest(null, true, List.of()))
        );
    }

    @Test
    void updateItemWhenItemNotFound() throws Exception {
        Long itemId = 100L;
        when(itemService.updateItem(eq(itemId), any(ItemRequest.class)))
                .thenThrow(new EntityNotFoundException("Menu with id 100 not found"));

        mockMvc.perform(put(BASE_URL + "/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteItem_Success() throws Exception {
        Long itemId = 1L;
        doNothing().when(itemService).deleteItem(itemId);

        mockMvc.perform(delete(BASE_URL + "/{id}", itemId))
                .andExpect(status().isNoContent());

        verify(itemService).deleteItem(itemId);
    }

    @Test
    void deleteItemWhenItemNotFound() throws Exception {
        Long itemId = 100L;
        doThrow(new EntityNotFoundException("Menu with id 100 not found"))
                .when(itemService).deleteItem(itemId);

        mockMvc.perform(delete(BASE_URL + "/{id}", itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAvailability_Success() throws Exception {
        Long itemId = 1L;
        boolean available = false;

        ItemResponse updatedResponse = new ItemResponse(itemId, "Pepperoni", false, 5L,
                itemResponse.options());

        when(itemService.updateAvailability(itemId, available)).thenReturn(updatedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/availability", itemId)
                        .param("available", String.valueOf(available)))
                .andExpect(status().isOk());
    }

    @Test
    void updateAvailabilityWhenItemNotFound() throws Exception {
        Long itemId = 100L;
        boolean available = true;

        when(itemService.updateAvailability(itemId, available))
                .thenThrow(new EntityNotFoundException("Menu with id 100 not found"));

        mockMvc.perform(patch(BASE_URL + "/{id}/availability", itemId)
                        .param("available", String.valueOf(available)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAvailabilityWithInvalidParam() throws Exception {
        Long itemId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/availability", itemId)
                        .param("available", "invalid"))
                .andExpect(status().isBadRequest());
    }
}