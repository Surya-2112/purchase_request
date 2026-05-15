package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.module.purchase.entity.PurchaseOrderHeader;
import java.util.Optional;
import com.module.purchase.repository.PurchaseOrderHeaderRepository;
import java.util.List;

@Service
public class PurchaseOrderHeaderService {
    
    @Autowired
    private PurchaseOrderHeaderRepository purchaseOrderHeaderRepository;

    public PurchaseOrderHeader savePurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader) {
        return purchaseOrderHeaderRepository.save(purchaseOrderHeader);
    }

    public PurchaseOrderHeader addPurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader) {
        return savePurchaseOrderHeader(purchaseOrderHeader);
    }

    public Optional<PurchaseOrderHeader> getPurchaseOrderHeaderById(Long id) {
        Optional<PurchaseOrderHeader>  existingPurchaseOrderHeader = purchaseOrderHeaderRepository.findById(id);
        if (!existingPurchaseOrderHeader.isPresent()) {
            throw new RuntimeException("Purchase order header not found with id: " + id);
        }
        return existingPurchaseOrderHeader;
    }

    public List<PurchaseOrderHeader> getAllPurchaseOrderHeaders() {
        return purchaseOrderHeaderRepository.findAll();
    }
}
