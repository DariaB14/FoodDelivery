package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.ItemMapper;
import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.entity.Item;
import com.example.fooddelivery.entity.ItemOption;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.exception.exceptions.EntityNotFoundException;
import com.example.fooddelivery.repository.ItemRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;
    private final ItemMapper itemMapper;

    public ItemResponse addItem(Long id, ItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Restaurant with id %d not found", id)));

        Item item = itemMapper.toEntity(request);
        item.setRestaurant(restaurant);

        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<ItemOption> options = itemMapper.toOptionEntities(request.getOptions());
            options.forEach(option -> option.setItem(item));
            item.setOptions(options);
        }

        Item saved = itemRepository.save(item);
        return itemMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getItems(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format("Restaurant with id %d not found", id));
        }

        List<Item> items = itemRepository.findByRestaurantId(id);
        return items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Menu with id %d not found", id)));

        itemMapper.update(request, item);

        if (request.getOptions() != null) {
            item.getOptions().clear();

            if (!request.getOptions().isEmpty()) {
                List<ItemOption> newOptions = itemMapper.toOptionEntities(request.getOptions());
                newOptions.forEach(option -> option.setItem(item));
                item.getOptions().addAll(newOptions);
            }
        }

        Item updated = itemRepository.save(item);
        return itemMapper.toDto(updated);
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Menu with id %d not found", id)));
        itemRepository.delete(item);
    }

    public ItemResponse updateAvailability(Long id, boolean available) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Menu with id %d not found", id)));

        item.setAvailable(available);
        Item updated = itemRepository.save(item);
        return itemMapper.toDto(updated);
    }
}
