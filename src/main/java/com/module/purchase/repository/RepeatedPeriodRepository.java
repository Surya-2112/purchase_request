package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.module.purchase.entity.RepeatedPeriod;

public interface RepeatedPeriodRepository extends JpaRepository<RepeatedPeriod, Long> {
    
}
