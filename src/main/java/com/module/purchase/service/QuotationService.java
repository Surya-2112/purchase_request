package com.module.purchase.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.QuotationDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.mapper.QuotationMapper;
import com.module.purchase.repository.DiscountTypeRepository;
import com.module.purchase.repository.QuotationLineRepository;
import com.module.purchase.repository.QuotationRepository;
import com.module.purchase.specification.QuotationSpecification;

@Service
@Transactional
public class QuotationService {

    @Autowired
    private  QuotationRepository quotationRepository;

    @Autowired
    private  QuotationLineRepository quotationLineRepository;

    @Autowired
    private  DiscountTypeRepository discountTypeRepository;

    @Autowired
    private  QuotationMapper quotationMapper;

    @Lazy
    @Autowired
    private PurchaseOrderHeaderService  purchaseOrderService;

    public Optional<Quotation> getQuotationById(Long id) {
        return quotationRepository.findById(id);
    }

    public List<Quotation> getAllQuotations() {
        return quotationRepository.findAll();
    }

    public Integer getCountByRFQ(RequestForQuotation rfq) {
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

    @Transactional
    public Quotation save(Quotation quotation) {
        
        return quotationRepository.save(quotation);
    }
    
    public Quotation addQuotation(Quotation quotation)
    {    
        if (quotation.getQuotationDate() == null) {
            quotation.setQuotationDate(LocalDate.now());
        }
        return  save(quotation);
    }

    public Quotation updateQuotation(Quotation quotation)
    {
        if(quotation.getStatus().equals(Status.APPROVED))
        {   
            purchaseOrderService.genratePurchaseOrder(quotation);
        }
        return save(quotation);
    }

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
        List<DiscountType> discounts= discountTypeRepository.findByQuotationLine(line);
        if(discounts==null)
        {
            discounts=new ArrayList<DiscountType>();
        }
        return discounts;
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

    public Page<QuotationDTO> getAllQuotations(QuotationDTO quotationDTO, int page, int size) {

        Specification<Quotation> spec = Specification
                .where(QuotationSpecification.hasQuotationId(quotationDTO.getId()))
                .and(QuotationSpecification.hasRequestForQuotationId(
                        quotationDTO.getRequestForQuotation() != null ? quotationDTO.getRequestForQuotation().getId() : null))
                .and(QuotationSpecification.hasVendor(quotationDTO.getVendor()))
                .and(QuotationSpecification.hasStatus(quotationDTO.getStatus()));

        Pageable pageable = PageRequest.of(page, size);
        Page<Quotation> quotationPage = quotationRepository.findAll(spec, pageable);

        // Using your mapper to transform the entities into DTOs cleanly
        return quotationPage.map(quotationMapper::toQuotationDTO);
    }
}
