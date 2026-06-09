package com.module.purchase.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;

@Repository
public interface QuotationLineRepository extends JpaRepository<QuotationLine, Long> {
    List<QuotationLine> findByQuotation(Quotation quotation);
    void deleteByQuotation(Quotation quotation);
}