package com.warehouse.service;

import com.warehouse.dto.StockUpdateDTO;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.model.StockMovement;
import com.warehouse.model.Variant;
import com.warehouse.repository.StockMovementRepository;
import com.warehouse.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final VariantRepository variantRepository;
    private final StockMovementRepository stockMovementRepository;

    public StockMovement addStock(StockUpdateDTO stockUpdateDTO) {
        log.info("Adding stock for variant ID: {}", stockUpdateDTO.getVariantId());

        Variant variant = variantRepository.findById(stockUpdateDTO.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + stockUpdateDTO.getVariantId()));

        // Validasi quantity
        if (stockUpdateDTO.getQuantity() == null || stockUpdateDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Update stock quantity
        int newStockQuantity = variant.getStockQuantity() + stockUpdateDTO.getQuantity();
        variant.setStockQuantity(newStockQuantity);
        variantRepository.save(variant);

        // Create stock movement
        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .movementType(StockMovement.MovementType.IN) // Hardcode sebagai IN untuk add-stock
                .quantity(stockUpdateDTO.getQuantity())
                .reason(stockUpdateDTO.getReason() != null ? stockUpdateDTO.getReason() : "Stock addition")
                .reference(stockUpdateDTO.getReference())
                .build();

        StockMovement savedMovement = stockMovementRepository.save(movement);
        log.info("Added {} units to variant ID: {}. New stock: {}",
                stockUpdateDTO.getQuantity(), stockUpdateDTO.getVariantId(), newStockQuantity);

        return savedMovement;
    }

    // Untuk remove-stock, buat method terpisah
    public StockMovement removeStock(StockUpdateDTO stockUpdateDTO) {
        log.info("Removing stock for variant ID: {}", stockUpdateDTO.getVariantId());

        Variant variant = variantRepository.findById(stockUpdateDTO.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + stockUpdateDTO.getVariantId()));

        if (stockUpdateDTO.getQuantity() == null || stockUpdateDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Check stock availability
        if (variant.getStockQuantity() < stockUpdateDTO.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock available. Available: " +
                    variant.getStockQuantity() + ", Requested: " + stockUpdateDTO.getQuantity());
        }

        // Update stock quantity
        int newStockQuantity = variant.getStockQuantity() - stockUpdateDTO.getQuantity();
        variant.setStockQuantity(newStockQuantity);
        variantRepository.save(variant);

        // Create stock movement
        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .movementType(StockMovement.MovementType.OUT) // Hardcode sebagai OUT untuk remove-stock
                .quantity(stockUpdateDTO.getQuantity())
                .reason(stockUpdateDTO.getReason() != null ? stockUpdateDTO.getReason() : "Stock removal")
                .reference(stockUpdateDTO.getReference())
                .build();

        StockMovement savedMovement = stockMovementRepository.save(movement);
        log.info("Removed {} units from variant ID: {}. New stock: {}",
                stockUpdateDTO.getQuantity(), stockUpdateDTO.getVariantId(), newStockQuantity);

        return savedMovement;
    }

    // Untuk adjust-stock (bisa positif atau negatif)
    public StockMovement adjustStock(StockUpdateDTO stockUpdateDTO) {
        log.info("Adjusting stock for variant ID: {}", stockUpdateDTO.getVariantId());

        Variant variant = variantRepository.findById(stockUpdateDTO.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + stockUpdateDTO.getVariantId()));

        if (stockUpdateDTO.getQuantity() == null) {
            throw new IllegalArgumentException("Quantity is required");
        }

        // Update stock quantity (bisa positif atau negatif)
        int newStockQuantity = variant.getStockQuantity() + stockUpdateDTO.getQuantity();
        if (newStockQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative. Adjustment would result in: " + newStockQuantity);
        }

        variant.setStockQuantity(newStockQuantity);
        variantRepository.save(variant);

        // Determine movement type based on quantity
        StockMovement.MovementType movementType = stockUpdateDTO.getQuantity() >= 0 ?
                StockMovement.MovementType.IN : StockMovement.MovementType.OUT;

        StockMovement movement = StockMovement.builder()
                .variant(variant)
                .movementType(StockMovement.MovementType.ADJUSTMENT)
                .quantity(stockUpdateDTO.getQuantity())
                .reason(stockUpdateDTO.getReason() != null ? stockUpdateDTO.getReason() : "Stock adjustment")
                .reference(stockUpdateDTO.getReference())
                .build();

        StockMovement savedMovement = stockMovementRepository.save(movement);
        log.info("Adjusted stock by {} units for variant ID: {}. New stock: {}",
                stockUpdateDTO.getQuantity(), stockUpdateDTO.getVariantId(), newStockQuantity);

        return savedMovement;
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getStockMovementHistory(Long variantId) {
        log.info("Fetching stock movement history for variant ID: {}", variantId);
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Variant not found with id: " + variantId);
        }
        return stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
    }

    @Transactional(readOnly = true)
    public Integer getCurrentStockLevel(Long variantId) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));
        return variant.getStockQuantity();
    }

    @Transactional(readOnly = true)
    public Integer getTotalStockIn(Long variantId) {
        return stockMovementRepository.getTotalStockIn(variantId);
    }

    @Transactional(readOnly = true)
    public Integer getTotalStockOut(Long variantId) {
        return stockMovementRepository.getTotalStockOut(variantId);
    }
}