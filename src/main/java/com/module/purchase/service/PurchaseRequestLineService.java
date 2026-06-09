package com.module.purchase.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.Status;
import com.module.purchase.repository.PurchaseRequestLineRepository;

@Service
@Transactional
public class PurchaseRequestLineService {
    
    @Autowired
    private PurchaseRequestLineRepository purchaseRequestLineRepository;

     public PurchaseRequestLine savePurchaseRequestLine(PurchaseRequestLine purchaseRequestLine) {
        return purchaseRequestLineRepository.save(purchaseRequestLine);
    }

    public Optional<PurchaseRequestLine> getPurchaseRequestLineById(Long id) {
        Optional<PurchaseRequestLine> existingPurchaseRequestLine = purchaseRequestLineRepository.findById(id);
        if (!existingPurchaseRequestLine.isPresent()) {
            throw new ResourceNotFoundException("Purchase request line not found with id: " + id);
        }
        return existingPurchaseRequestLine;
    }

    public List<PurchaseRequestLine> getPurchaseRequestLineByHeader(PurchaseRequestHeader header)
    {
        return purchaseRequestLineRepository.findByPurchaseRequestHeader(header);
    }

    public List<PurchaseRequestLine> getApprovedLinesAvailableForRfq()
    {
        return purchaseRequestLineRepository.findAvailableApprovedLinesForRfq(Status.DRAFT);
    }

    public PurchaseRequestLine updatePurchaseRequestLine(PurchaseRequestLine purchaseRequestLine) {
        System.out.println(purchaseRequestLine.getRepeatableId());
        return savePurchaseRequestLine(purchaseRequestLine);
    }

    public PurchaseRequestLine addPurchaseRequestLine(PurchaseRequestLine purchaseRequestLine) {
        return savePurchaseRequestLine(purchaseRequestLine);
    }

    public List<PurchaseRequestLine> getAllPurchaseRequestLines() {
        return purchaseRequestLineRepository.findAll();
    }

    public void deletePurchaseRequestLineById(Long id)
    {
        purchaseRequestLineRepository.deleteById(id);
    }

    public void deleteAllLine(PurchaseRequestHeader header)
    {
        List<PurchaseRequestLine> lines=getPurchaseRequestLineByHeader(header);
        for(PurchaseRequestLine line:lines)
        {
            deletePurchaseRequestLineById(line.getId());
        }
    }

    public List<PurchaseRequestLine> getRequestForQuotation(RequestForQuotation rfq)
    {
        return purchaseRequestLineRepository.findByRequestForQuotation(rfq);
    }
}
