package com.warehouse.service;

import com.warehouse.dto.ItemDTO;
import com.warehouse.dto.VariantDTO;
import com.warehouse.exception.DuplicateResourceException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.Item;
import com.warehouse.model.StockMovement;
import com.warehouse.model.Variant;
import com.warehouse.repository.ItemRepository;
import com.warehouse.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final VariantService variantService;
    private final StockMovementRepository stockMovementRepository;

    public ItemDTO createItem(ItemDTO itemDTO) {
        log.info("Creating new item: {}", itemDTO.getName());

        if (itemRepository.existsByName(itemDTO.getName())) {
            throw new DuplicateResourceException("Item with name '" + itemDTO.getName() + "' already exists");
        }

        Item item = Item.builder()
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .basePrice(itemDTO.getBasePrice())
                .build();

        Item savedItem = itemRepository.save(item);
        log.info("Created item with ID: {}", savedItem.getId());

        return convertToDTO(savedItem);
    }

    public ItemDTO createItemWithVariants(ItemDTO itemDTO) {
        log.info("Creating new item with variants: {}", itemDTO.getName());

        if (itemRepository.existsByName(itemDTO.getName())) {
            throw new DuplicateResourceException("Item with name '" + itemDTO.getName() + "' already exists");
        }

        Item item = Item.builder()
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .basePrice(itemDTO.getBasePrice())
                .build();

        Item savedItem = itemRepository.save(item);
        log.info("Created item with ID: {}", savedItem.getId());

        if (itemDTO.getVariants() != null && !itemDTO.getVariants().isEmpty()) {
            for (VariantDTO variantDTO : itemDTO.getVariants()) {
                // Check if variant with same SKU already exists
                if (!variantService.existsBySku(variantDTO.getSku())) {
                    Variant variant = Variant.builder()
                            .item(savedItem)
                            .sku(variantDTO.getSku())
                            .size(variantDTO.getSize())
                            .color(variantDTO.getColor())
                            .material(variantDTO.getMaterial())
                            .price(variantDTO.getPrice())
                            .stockQuantity(variantDTO.getStockQuantity())
                            .minStockLevel(variantDTO.getMinStockLevel())
                            .build();

                    Variant savedVariant = variantService.saveVariantDirectly(variant);

                    if (variantDTO.getStockQuantity() > 0) {
                        StockMovement movement = StockMovement.builder()
                                .variant(savedVariant)
                                .movementType(StockMovement.MovementType.IN)
                                .quantity(variantDTO.getStockQuantity())
                                .reason("Initial stock")
                                .build();
                        stockMovementRepository.save(movement);
                    }
                }
            }
        }

        Item itemWithVariants = itemRepository.findByIdWithVariants(savedItem.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found after creation"));

        return convertToDTO(itemWithVariants);
    }

    @Transactional(readOnly = true)
    public List<ItemDTO> getAllItems() {
        log.info("Fetching all items");
        return itemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemDTO getItemById(Long id) {
        log.info("Fetching item by ID: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return convertToDTO(item);
    }

    public ItemDTO updateItem(Long id, ItemDTO itemDTO) {
        log.info("Updating item with ID: {}", id);
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        if (!existingItem.getName().equals(itemDTO.getName()) &&
                itemRepository.existsByName(itemDTO.getName())) {
            throw new DuplicateResourceException("Item with name '" + itemDTO.getName() + "' already exists");
        }

        existingItem.setName(itemDTO.getName());
        existingItem.setDescription(itemDTO.getDescription());
        existingItem.setBasePrice(itemDTO.getBasePrice());

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Updated item with ID: {}", updatedItem.getId());

        return convertToDTO(updatedItem);
    }

    public void deleteItem(Long id) {
        log.info("Deleting item with ID: {}", id);
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
        log.info("Deleted item with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ItemDTO> searchItemsByName(String name) {
        log.info("Searching items by name: {}", name);
        return itemRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ItemDTO convertToDTO(Item item) {
        ItemDTO dto = ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();

        if (item.getVariants() != null) {
            dto.setVariants(item.getVariants().stream()
                    .map(variantService::convertToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}