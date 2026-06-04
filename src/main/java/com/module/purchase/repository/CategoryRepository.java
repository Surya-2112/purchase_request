package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.Category;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findByCategoryName(String categoryName);
}
