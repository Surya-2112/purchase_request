package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.module.purchase.repository.PurchaseOrderLineRepository;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import java.util.Optional;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseOrderLineService {
    
    @Autowired
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    public PurchaseOrderLine savePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        return purchaseOrderLineRepository.save(purchaseOrderLine);
    }

    public Optional<PurchaseOrderLine> getPurchaseOrderLineById(Long id) {
        Optional<PurchaseOrderLine> existingPurchaseOrderLine = purchaseOrderLineRepository.findById(id);
        if (!existingPurchaseOrderLine.isPresent()) {
            throw new ResourceNotFoundException("Purchase order line not found with id: " + id);
        }
        return existingPurchaseOrderLine;
    }
   

    public PurchaseOrderLine addPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        return savePurchaseOrderLine(purchaseOrderLine);
    }

    public List<PurchaseOrderLine> getAllPurchaseOrderLines() {
        return purchaseOrderLineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderLine> getPurchaseOrderLineByHeader(PurchaseOrderHeader purchaseOrderHeader) {
        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderHeader(purchaseOrderHeader);
        
        for (PurchaseOrderLine line : lines) {
            if (line.getPurchaseRequestLines() != null) {
                line.getPurchaseRequestLines().size(); 
            }
        }
        return lines;
    }

}
