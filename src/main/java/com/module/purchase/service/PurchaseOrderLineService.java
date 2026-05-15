package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.module.purchase.repository.PurchaseOrderLineRepository;
import com.module.purchase.entity.PurchaseOrderLine;
import java.util.Optional;
import java.util.List;

@Service
public class PurchaseOrderLineService {
    
    @Autowired
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    public PurchaseOrderLine savePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        return purchaseOrderLineRepository.save(purchaseOrderLine);
    }

    public Optional<PurchaseOrderLine> getPurchaseOrderLineById(Long id) {
        Optional<PurchaseOrderLine> existingPurchaseOrderLine = purchaseOrderLineRepository.findById(id);
        if (!existingPurchaseOrderLine.isPresent()) {
            throw new RuntimeException("Purchase order line not found with id: " + id);
        }
        return existingPurchaseOrderLine;
    }

    public PurchaseOrderLine addPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        return savePurchaseOrderLine(purchaseOrderLine);
    }

    public List<PurchaseOrderLine> getAllPurchaseOrderLines() {
        return purchaseOrderLineRepository.findAll();
    }

}
