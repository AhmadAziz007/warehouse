package com.warehouse.repository;

import com.warehouse.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {
    Optional<Variant> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Variant> findByItemId(Long itemId);

    @Query("SELECT v FROM Variant v WHERE v.item.id = :itemId AND v.size = :size AND v.color = :color AND v.material = :material")
    Optional<Variant> findByItemIdAndAttributes(@Param("itemId") Long itemId,
                                                @Param("size") String size,
                                                @Param("color") String color,
                                                @Param("material") String material);

    @Query("SELECT v FROM Variant v WHERE v.stockQuantity <= v.minStockLevel AND v.stockQuantity > 0")
    List<Variant> findLowStockVariants();

    List<Variant> findByStockQuantityGreaterThan(Integer stockQuantity);

    @Query("SELECT v FROM Variant v WHERE v.stockQuantity = 0")
    List<Variant> findOutOfStock();
}