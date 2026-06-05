package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.ItemVariant;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {
    
}
