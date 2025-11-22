package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.ItemMapper;
import com.example.fooddelivery.dto.request.ItemOptionRequest;
import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.response.ItemOptionResponse;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.entity.Item;
import com.example.fooddelivery.entity.ItemOption;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.enums.ItemSize;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.repository.ItemRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private final Long ITEM_ID = 1L;
    private final Long RESTAURANT_ID = 1L;
    private final Long NON_EXISTENT_ITEM_ID = 999L;
    private final Long NON_EXISTENT_RESTAURANT_ID = 999L;

    private ItemRequest itemRequest;
    private ItemResponse itemResponse;
    private ItemOptionRequest itemOptionRequest;
    private ItemOptionResponse itemOptionResponse;
    private Item item;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        itemOptionRequest = new ItemOptionRequest(ItemSize.LARGE, new BigDecimal("1000.00"), 20);
        itemRequest = new ItemRequest("Pepperoni", true, List.of(itemOptionRequest));

        itemOptionResponse = new ItemOptionResponse(1L, ItemSize.LARGE, new BigDecimal("1000.00"), 20);
        itemResponse = new ItemResponse(1L, "Pepperoni", true, RESTAURANT_ID, List.of(itemOptionResponse));

        restaurant = new Restaurant();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setName("Pizza");

        item = new Item();
        item.setId(ITEM_ID);
        item.setName("Pepperoni");
        item.setAvailable(true);
        item.setRestaurant(restaurant);
    }

    @Test
    void addItem_Success() {
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(itemMapper.toEntity(itemRequest)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.addItem(RESTAURANT_ID, itemRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(ITEM_ID);

        verify(itemRepository).save(item);
    }

    @Test
    void addItemWithOptions() {
        ItemOption itemOption = new ItemOption(ItemSize.LARGE, new BigDecimal("1000.00"), 20, item);
        item.setOptions(List.of(itemOption));

        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(itemMapper.toEntity(itemRequest)).thenReturn(item);
        when(itemMapper.toOptionEntities(itemRequest.getOptions())).thenReturn(List.of(itemOption));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.addItem(RESTAURANT_ID, itemRequest);

        assertThat(result).isNotNull();
        assertThat(result.options()).hasSize(1);

        verify(itemRepository).save(item);
    }

    @Test
    void addItemWhenRestaurantNotFound() {
        when(restaurantRepository.findById(NON_EXISTENT_RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addItem(NON_EXISTENT_RESTAURANT_ID, itemRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Restaurant with id 999 not found");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItems_Success() {
        when(restaurantRepository.existsById(RESTAURANT_ID)).thenReturn(true);
        when(itemRepository.findByRestaurantId(RESTAURANT_ID)).thenReturn(List.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemResponse);

        List<ItemResponse> result = itemService.getItems(RESTAURANT_ID);

        assertThat(result).hasSize(1);

        verify(itemRepository).findByRestaurantId(RESTAURANT_ID);
    }

    @Test
    void getItemsWhenRestaurantNotFound() {
        when(restaurantRepository.existsById(NON_EXISTENT_RESTAURANT_ID)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getItems(NON_EXISTENT_RESTAURANT_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Restaurant with id 999 not found");

        verify(itemRepository, never()).findByRestaurantId(any());
    }

    @Test
    void updateItem_Success() {
        ItemRequest updateRequest = new ItemRequest("PizzaBig", false, List.of(itemOptionRequest));

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.updateItem(ITEM_ID, updateRequest);

        assertThat(result).isNotNull();

        verify(itemMapper).update(updateRequest, item);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItemWithNewOptions_Success() {
        ItemRequest updateRequest = new ItemRequest("PizzaBig", false, List.of(itemOptionRequest));
        ItemOption newOption = new ItemOption(ItemSize.MEDIUM, new BigDecimal("800.00"), 15, item);

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemMapper.toOptionEntities(updateRequest.getOptions())).thenReturn(List.of(newOption));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.updateItem(ITEM_ID, updateRequest);

        assertThat(result).isNotNull();

        verify(itemRepository).save(item);
    }

    @Test
    void updateItemWhenItemNotFound() {
        when(itemRepository.findById(NON_EXISTENT_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(NON_EXISTENT_ITEM_ID, itemRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Menu with id 999 not found");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void deleteItem_Success() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        itemService.deleteItem(ITEM_ID);

        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItemWhenItemNotFound() {
        when(itemRepository.findById(NON_EXISTENT_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(NON_EXISTENT_ITEM_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Menu with id 999 not found");

        verify(itemRepository, never()).delete(any());
    }

    @Test
    void updateAvailability_Success() {
        item.setAvailable(false);

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.updateAvailability(ITEM_ID, true);

        assertThat(result).isNotNull();
        assertThat(item.isAvailable()).isTrue();

        verify(itemRepository).save(item);
    }

    @Test
    void updateAvailabilityWhenItemNotFound() {
        when(itemRepository.findById(NON_EXISTENT_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateAvailability(NON_EXISTENT_ITEM_ID, true))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Menu with id 999 not found");

        verify(itemRepository, never()).save(any());
    }
}