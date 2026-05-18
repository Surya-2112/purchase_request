package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.module.purchase.repository.PurchaseRequestLineRepository;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PurchaseRequestLineService {
    
    @Autowired
    private PurchaseRequestLineRepository purchaseRequestLineRepository;

     public PurchaseRequestLine savePurchaseRequestLine(PurchaseRequestLine purchaseRequestLine) {
        return purchaseRequestLineRepository.save(purchaseRequestLine);
    }

    public Optional<PurchaseRequestLine> getPurchaseRequestLineById(Long id) {
        Optional<PurchaseRequestLine> existingPurchaseRequestLine = purchaseRequestLineRepository.findById(id);
        if (!existingPurchaseRequestLine.isPresent()) {
            throw new RuntimeException("Purchase request line not found with id: " + id);
        }
        return existingPurchaseRequestLine;
    }

    public List<PurchaseRequestLine> getPurchaseRequestLineByHeader(PurchaseRequestHeader header)
    {
        return purchaseRequestLineRepository.findByPurchaseRequestHeader(header);
    }

    public PurchaseRequestLine addPurchaseRequestLine(PurchaseRequestLine purchaseRequestLine) {
        return savePurchaseRequestLine(purchaseRequestLine);
    }

    public List<PurchaseRequestLine> getAllPurchaseRequestLines() {
        return purchaseRequestLineRepository.findAll();
    }
}
