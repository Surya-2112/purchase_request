package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.module.purchase.entity.AssigningConfig;

public interface AssigningConfigRepository extends JpaRepository<AssigningConfig, Long>,JpaSpecificationExecutor<AssigningConfig> {
    
}
