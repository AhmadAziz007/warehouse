package com.warehouse.controller;

import com.warehouse.dto.StockUpdateDTO;
import com.warehouse.model.StockMovement;
import com.warehouse.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/add-stock")
    public ResponseEntity<StockMovement> addStock(@Valid @RequestBody StockUpdateDTO stockUpdateDTO) {
        StockMovement movement = inventoryService.addStock(stockUpdateDTO);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/remove-stock")
    public ResponseEntity<StockMovement> removeStock(@Valid @RequestBody StockUpdateDTO stockUpdateDTO) {
        StockMovement movement = inventoryService.removeStock(stockUpdateDTO);
        return ResponseEntity.ok(movement);
    }

    @PostMapping("/adjust-stock")
    public ResponseEntity<StockMovement> adjustStock(@Valid @RequestBody StockUpdateDTO stockUpdateDTO) {
        StockMovement movement = inventoryService.adjustStock(stockUpdateDTO);
        return ResponseEntity.ok(movement);
    }

    @GetMapping("/{variantId}/movements")
    public ResponseEntity<List<StockMovement>> getStockMovementHistory(@PathVariable Long variantId) {
        List<StockMovement> movements = inventoryService.getStockMovementHistory(variantId);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/{variantId}/current-stock")
    public ResponseEntity<Integer> getCurrentStockLevel(@PathVariable Long variantId) {
        Integer stockLevel = inventoryService.getCurrentStockLevel(variantId);
        return ResponseEntity.ok(stockLevel);
    }

    @GetMapping("/{variantId}/total-in")
    public ResponseEntity<Integer> getTotalStockIn(@PathVariable Long variantId) {
        Integer totalIn = inventoryService.getTotalStockIn(variantId);
        return ResponseEntity.ok(totalIn);
    }

    @GetMapping("/{variantId}/total-out")
    public ResponseEntity<Integer> getTotalStockOut(@PathVariable Long variantId) {
        Integer totalOut = inventoryService.getTotalStockOut(variantId);
        return ResponseEntity.ok(totalOut);
    }
}