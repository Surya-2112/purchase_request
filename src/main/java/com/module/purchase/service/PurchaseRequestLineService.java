package com.module.purchase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.repository.PurchaseRequestLineRepository;

@Service
@Transactional
public class PurchaseRequestLineService {
    
    @Autowired
    private PurchaseRequestLineRepository purchaseRequestLineRepository;

    @Lazy
    @Autowired
    private RepeatedPeriodService repeatedPeriodService;

    @Autowired
    private ItemService itemService;

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

    public PurchaseRequestLine updatePurchaseRequestLine(PurchaseRequestLine purchaseRequestLine,Employee employee) {

        if(purchaseRequestLine.getStatus()==Status.CANCELLED && purchaseRequestLine.getRepeatableId()!=null)
        {
            RepeatedPeriod period=repeatedPeriodService.getRepeatedPeriodById(purchaseRequestLine.getId()).get();
            period.setStatus(RequestForQuotationStatus.CANCELLED);
           repeatedPeriodService.updateRepeatedPeriod(period,employee);
        }
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
            if(line.getRepeatableId()!=null)
            {
                repeatedPeriodService.deleteByReferTypeAndReferId(RepeatedPeriodReferType.PURCHASE_REQUEST_LINE, line.getId());
            }
        }
    }

    public List<PurchaseRequestLine> getRequestForQuotation(RequestForQuotation rfq)
    {
        return purchaseRequestLineRepository.findByRequestForQuotation(rfq);
    }

    public List<PurchaseRequestLine> getApprovedPurchaseLinesAvailableForRfq(ItemVariant itemVariant)
    {  
       return purchaseRequestLineRepository.findByItemVariantAndRequestForQuotationIsNullAndStatusIn(itemVariant,List.of(Status.APPROVED,Status.PARTIALLY_APPROVED));
    }

    public List<PurchaseRequestLine> getPurchaseLinesByCategory(Category category)
    {    List<PurchaseRequestLine> matchedLines=new ArrayList<>();

        for(Item item : itemService.getItemByCategory(category))
        {
            for(ItemVariant itemVariant:item.getItemVariants())
            {
                matchedLines.addAll(getApprovedPurchaseLinesAvailableForRfq(itemVariant));
            }
        }
        return matchedLines;
    }
}
