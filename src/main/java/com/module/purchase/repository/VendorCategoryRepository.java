package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.VendorCategory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface VendorCategoryRepository extends JpaRepository<VendorCategory, Long>, JpaSpecificationExecutor<VendorCategory> {
    Optional<VendorCategory> findByCategoryName(String categoryName);
}
