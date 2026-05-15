package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.module.purchase.entity.Item;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> ,JpaSpecificationExecutor<Item> {
    
    Optional<Item> findByItemCode(String itemCode);
}
