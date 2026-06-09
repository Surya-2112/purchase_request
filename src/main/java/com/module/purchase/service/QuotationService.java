package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.Status;
import com.module.purchase.repository.DiscountTypeRepository;
import com.module.purchase.repository.QuotationLineRepository;
import com.module.purchase.repository.QuotationRepository;

@Service
@Transactional(readOnly = true)
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final QuotationLineRepository quotationLineRepository;
    private final DiscountTypeRepository discountTypeRepository;

    public QuotationService(QuotationRepository quotationRepository,
                            QuotationLineRepository quotationLineRepository,
                            
                            DiscountTypeRepository discountTypeRepository) {
        this.quotationRepository = quotationRepository;
        this.quotationLineRepository = quotationLineRepository;
        this.discountTypeRepository = discountTypeRepository;
    }
    public Optional<Quotation> getQuotationById(Long id) {
        return quotationRepository.findById(id);
    }

    public List<Quotation> getAllQuotations() {
        return quotationRepository.findAll();
    }

    public Integer getCountByRFQ(RequestForQuotation rfq)
    {
        return getQuotationsByRfq(rfq).size();
    }

    public List<Quotation> getQuotationsByRfq(RequestForQuotation rfq) {
        return quotationRepository.findByRequestForQuotation(rfq);
    }

    public List<Quotation> getQuotationsByVendor(Vendor vendor) {
        return quotationRepository.findByVendor(vendor);
    }

    public boolean hasVendorSubmittedForRfq(Long rfqId, Long vendorId) {
        return !quotationRepository.findByRfqAndVendor(rfqId, vendorId).isEmpty();
    }

    /**
     * Commits a completely new Quotation Document to the database system along with timestamp markers.
     */
    @Transactional
    public Quotation saveQuotation(Quotation quotation) {
        if (quotation.getQuotationDate() == null) {
            quotation.setQuotationDate(LocalDate.now());
        }
        return quotationRepository.save(quotation);
    }

    /**
     * Completely removes a draft quotation along with its cascade children components 
     * to prevent orphaned foreign keys.
     */
    @Transactional
    public void deleteQuotation(Long id) {
        quotationRepository.findById(id).ifPresent(quotation -> {
            if (quotation.getStatus() == Status.DRAFT) {
                List<QuotationLine> lines = quotationLineRepository.findByQuotation(quotation);
                for (QuotationLine line : lines) {
                    discountTypeRepository.deleteByQuotationLine(line);
                }
                quotationLineRepository.deleteByQuotation(quotation);
                quotationRepository.delete(quotation);
            } else {
                throw new IllegalStateException("Compliance Block: Finalized quotations cannot be deleted from the system ledger.");
            }
        });
    }

    public List<QuotationLine> getLinesByQuotation(Quotation quotation) {
        return quotationLineRepository.findByQuotation(quotation);
    }

    @Transactional
    public QuotationLine saveQuotationLine(QuotationLine line) {
        return quotationLineRepository.save(line);
    }

    @Transactional
    public void clearLinesByQuotation(Quotation quotation) {
        List<QuotationLine> lines = quotationLineRepository.findByQuotation(quotation);
        for (QuotationLine line : lines) {
            discountTypeRepository.deleteByQuotationLine(line);
        }
        quotationLineRepository.deleteByQuotation(quotation);
    }

    public List<DiscountType> getDiscountsByLine(QuotationLine line) {
        return discountTypeRepository.findByQuotationLine(line);
    }

    @Transactional
    public DiscountType saveDiscountType(DiscountType discountType) {
        // Validation check to make sure bounds parameters logic doesn't crash calculations engines
        if (discountType.getFromQuantity() >= discountType.getToQuantity()) {
            throw new IllegalArgumentException("Validation Fault: 'From Quantity' slab boundary must sit below 'To Quantity' metrics.");
        }
        return discountTypeRepository.save(discountType);
    }

    @Transactional
    public void deleteDiscountType(Long id) {
        discountTypeRepository.deleteById(id);
    }

    public boolean isDuplicateSubmission(Long rfqId, Long vendorId) {
    if (rfqId == null || vendorId == null) {
        return false;
    }
    return !quotationRepository.findByRfqAndVendor(rfqId, vendorId).isEmpty();
}
}