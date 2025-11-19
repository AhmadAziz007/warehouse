package com.warehouse.repository;

import com.warehouse.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.variant.id = :variantId AND sm.createdAt BETWEEN :startDate AND :endDate ORDER BY sm.createdAt DESC")
    List<StockMovement> findByVariantIdAndDateRange(@Param("variantId") Long variantId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm WHERE sm.variant.id = :variantId AND sm.movementType = 'IN'")
    Integer getTotalStockIn(@Param("variantId") Long variantId);

    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm WHERE sm.variant.id = :variantId AND sm.movementType = 'OUT'")
    Integer getTotalStockOut(@Param("variantId") Long variantId);
}