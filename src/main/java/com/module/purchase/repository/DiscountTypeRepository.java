package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.DiscountType;

public interface DiscountTypeRepository extends JpaRepository<DiscountType, Long> {
    
}
