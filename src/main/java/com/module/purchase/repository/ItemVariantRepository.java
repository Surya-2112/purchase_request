package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long>, JpaSpecificationExecutor<ItemVariant> {
    
    List<ItemVariant> findByItem(Item item);
}
