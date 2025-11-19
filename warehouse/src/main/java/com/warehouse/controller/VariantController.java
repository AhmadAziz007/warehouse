package com.warehouse.controller;

import com.warehouse.dto.VariantDTO;
import com.warehouse.service.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VariantController {

    private final VariantService variantService;

    @PostMapping
    public ResponseEntity<VariantDTO> createVariant(@Valid @RequestBody VariantDTO variantDTO) {
        VariantDTO createdVariant = variantService.createVariant(variantDTO);
        return new ResponseEntity<>(createdVariant, HttpStatus.CREATED);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<VariantDTO>> getVariantsByItemId(@PathVariable Long itemId) {
        List<VariantDTO> variants = variantService.getVariantsByItemId(itemId);
        return ResponseEntity.ok(variants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantDTO> getVariantById(@PathVariable Long id) {
        VariantDTO variant = variantService.getVariantById(id);
        return ResponseEntity.ok(variant);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<VariantDTO> getVariantBySku(@PathVariable String sku) {
        VariantDTO variant = variantService.getVariantBySku(sku);
        return ResponseEntity.ok(variant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VariantDTO> updateVariant(@PathVariable Long id, @Valid @RequestBody VariantDTO variantDTO) {
        VariantDTO updatedVariant = variantService.updateVariant(id, variantDTO);
        return ResponseEntity.ok(updatedVariant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        variantService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<VariantDTO>> getLowStockVariants() {
        List<VariantDTO> variants = variantService.getLowStockVariants();
        return ResponseEntity.ok(variants);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<VariantDTO>> getOutOfStockVariants() {
        List<VariantDTO> variants = variantService.getOutOfStockVariants();
        return ResponseEntity.ok(variants);
    }

    @GetMapping("/in-stock")
    public ResponseEntity<List<VariantDTO>> getInStockVariants() {
        List<VariantDTO> variants = variantService.getInStockVariants();
        return ResponseEntity.ok(variants);
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<Void> reserveStock(@PathVariable Long id, @RequestParam Integer quantity) {
        variantService.reserveStock(id, quantity);
        return ResponseEntity.ok().build();
    }
}