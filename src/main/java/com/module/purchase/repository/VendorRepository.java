package com.module.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.module.purchase.entity.Category;
import com.module.purchase.entity.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, Long>,JpaSpecificationExecutor<Vendor> {

    Optional<Vendor> findByVendorEmail(String vendorEmail);

    @Query("""
       SELECT v
       FROM Vendor v
       WHERE v.users IS NULL
       """)
    List<Vendor> findVendorsWithoutUser();

    @Query("SELECT v FROM Vendor v JOIN v.categories c WHERE c = :category and v.active = true ")
    List<Vendor> findByCategoriesContaining(@Param("category") Category category);
}
