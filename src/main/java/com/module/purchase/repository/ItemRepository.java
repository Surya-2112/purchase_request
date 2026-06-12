package com.module.purchase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.module.purchase.entity.Item;
import com.module.purchase.entity.Category;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> ,JpaSpecificationExecutor<Item> {
    
    Optional<Item> findByItemCode(String itemCode);

    List<Item> findByCategory(Category category);
}
