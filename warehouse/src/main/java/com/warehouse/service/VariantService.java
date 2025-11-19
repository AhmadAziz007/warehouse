package com.warehouse.service;

import com.warehouse.dto.VariantDTO;
import com.warehouse.exception.DuplicateResourceException;
import com.warehouse.exception.InsufficientStockException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.Item;
import com.warehouse.model.StockMovement;
import com.warehouse.model.Variant;
import com.warehouse.repository.ItemRepository;
import com.warehouse.repository.StockMovementRepository;
import com.warehouse.repository.VariantRepository;
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
public class VariantService {

    private final VariantRepository variantRepository;
    private final ItemRepository itemRepository;
    private final StockMovementRepository stockMovementRepository;

    public VariantDTO createVariant(VariantDTO variantDTO) {
        log.info("Creating new variant with SKU: {}", variantDTO.getSku());

        if (variantRepository.existsBySku(variantDTO.getSku())) {
            throw new DuplicateResourceException("Variant with SKU '" + variantDTO.getSku() + "' already exists");
        }

        Item item = itemRepository.findById(variantDTO.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + variantDTO.getItemId()));

        variantRepository.findByItemIdAndAttributes(
                variantDTO.getItemId(),
                variantDTO.getSize(),
                variantDTO.getColor(),
                variantDTO.getMaterial()
        ).ifPresent(v -> {
            throw new DuplicateResourceException("Variant with these attributes already exists for this item");
        });

        Variant variant = Variant.builder()
                .item(item)
                .sku(variantDTO.getSku())
                .size(variantDTO.getSize())
                .color(variantDTO.getColor())
                .material(variantDTO.getMaterial())
                .price(variantDTO.getPrice())
                .stockQuantity(variantDTO.getStockQuantity())
                .minStockLevel(variantDTO.getMinStockLevel())
                .build();

        Variant savedVariant = variantRepository.save(variant);

        if (variantDTO.getStockQuantity() > 0) {
            StockMovement movement = StockMovement.builder()
                    .variant(savedVariant)
                    .movementType(StockMovement.MovementType.IN)
                    .quantity(variantDTO.getStockQuantity())
                    .reason("Initial stock")
                    .build();
            stockMovementRepository.save(movement);
        }

        log.info("Created variant with ID: {}", savedVariant.getId());
        return convertToDTO(savedVariant);
    }

    @Transactional(readOnly = true)
    public List<VariantDTO> getVariantsByItemId(Long itemId) {
        log.info("Fetching variants for item ID: {}", itemId);
        return variantRepository.findByItemId(itemId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VariantDTO getVariantById(Long id) {
        log.info("Fetching variant by ID: {}", id);
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));
        return convertToDTO(variant);
    }

    @Transactional(readOnly = true)
    public VariantDTO getVariantBySku(String sku) {
        log.info("Fetching variant by SKU: {}", sku);
        Variant variant = variantRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with SKU: " + sku));
        return convertToDTO(variant);
    }

    public VariantDTO updateVariant(Long id, VariantDTO variantDTO) {
        log.info("Updating variant with ID: {}", id);
        Variant existingVariant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + id));

        if (!existingVariant.getSku().equals(variantDTO.getSku()) &&
                variantRepository.existsBySku(variantDTO.getSku())) {
            throw new DuplicateResourceException("Variant with SKU '" + variantDTO.getSku() + "' already exists");
        }

        existingVariant.setSku(variantDTO.getSku());
        existingVariant.setSize(variantDTO.getSize());
        existingVariant.setColor(variantDTO.getColor());
        existingVariant.setMaterial(variantDTO.getMaterial());
        existingVariant.setPrice(variantDTO.getPrice());
        existingVariant.setMinStockLevel(variantDTO.getMinStockLevel());

        Variant updatedVariant = variantRepository.save(existingVariant);
        log.info("Updated variant with ID: {}", updatedVariant.getId());
        return convertToDTO(updatedVariant);
    }

    public void deleteVariant(Long id) {
        log.info("Deleting variant with ID: {}", id);
        if (!variantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Variant not found with id: " + id);
        }
        variantRepository.deleteById(id);
        log.info("Deleted variant with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<VariantDTO> getLowStockVariants() {
        log.info("Fetching low stock variants");
        List<Variant> lowStockVariants = variantRepository.findLowStockVariants();

        log.info("Found {} low stock variants", lowStockVariants.size());

        return lowStockVariants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VariantDTO> getOutOfStockVariants() {
        log.info("Fetching out of stock variants");
        return variantRepository.findOutOfStock().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VariantDTO> getInStockVariants() {
        log.info("Fetching in stock variants");
        return variantRepository.findByStockQuantityGreaterThan(0).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void reserveStock(Long variantId, Integer quantity) {
        log.info("Reserving {} units for variant ID: {}", quantity, variantId);
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        if (variant.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for variant " + variant.getSku() +
                            ". Available: " + variant.getStockQuantity() + ", Requested: " + quantity
            );
        }

        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        variantRepository.save(variant);

        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .movementType(StockMovement.MovementType.OUT)
                .quantity(quantity)
                .reason("Sale reservation")
                .build();
        stockMovementRepository.save(movement);

        log.info("Reserved {} units for variant ID: {}", quantity, variantId);
    }

    public VariantDTO convertToDTO(Variant variant) {
        return VariantDTO.builder()
                .id(variant.getId())
                .itemId(variant.getItem().getId())
                .sku(variant.getSku())
                .size(variant.getSize())
                .color(variant.getColor())
                .material(variant.getMaterial())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .minStockLevel(variant.getMinStockLevel())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }

    public boolean existsBySku(String sku) {
        return variantRepository.existsBySku(sku);
    }

    public Variant saveVariantDirectly(Variant variant) {
        return variantRepository.save(variant);
    }
}