package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.Vendor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long>,JpaSpecificationExecutor<Vendor> {

    Optional<Vendor> findByVendorEmail(String vendorEmail);

    @Query("""
       SELECT v
       FROM Vendor v
       WHERE v.users IS NULL
       """)
    List<Vendor> findVendorsWithoutUser();
}
