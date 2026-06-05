package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.Unit;

public interface UnitRepository extends JpaRepository<Unit, Integer> {
    
}
