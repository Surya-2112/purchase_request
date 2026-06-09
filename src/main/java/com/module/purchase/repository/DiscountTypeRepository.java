package com.module.purchase.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.QuotationLine;

@Repository
public interface DiscountTypeRepository extends JpaRepository<DiscountType, Long> {
    List<DiscountType> findByQuotationLine(QuotationLine quotationLine);
    void deleteByQuotationLine(QuotationLine quotationLine);
}