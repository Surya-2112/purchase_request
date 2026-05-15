package com.module.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.module.purchase.entity.Vendor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long>,JpaSpecificationExecutor<Vendor> {
    Optional<Vendor> findByVendorEmail(String vendorEmail);
}
