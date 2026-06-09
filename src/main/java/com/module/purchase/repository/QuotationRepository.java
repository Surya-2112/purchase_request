package com.module.purchase.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.Status;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long>, JpaSpecificationExecutor<Quotation> {
    
    List<Quotation> findByRequestForQuotation(RequestForQuotation rfq);
    
    List<Quotation> findByVendor(Vendor vendor);
    
    List<Quotation> findByVendorAndStatus(Vendor vendor, Status status);

    @Query("SELECT q FROM Quotation q WHERE q.requestForQuotation.id = :rfqId AND q.vendor.id = :vendorId")
    List<Quotation> findByRfqAndVendor(@Param("rfqId") Long rfqId, @Param("vendorId") Long vendorId);
}