package com.warehouse.repository;

import com.warehouse.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Item> findByNameContainingIgnoreCase(String name);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.variants WHERE i.id = :id")
    Optional<Item> findByIdWithVariants(@Param("id") Long id);
}