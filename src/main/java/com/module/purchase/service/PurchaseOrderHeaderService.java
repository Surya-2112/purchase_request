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

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Status;
import com.module.purchase.mapper.PurchaseOrderMapper;
import com.module.purchase.repository.PurchaseOrderHeaderRepository;
import com.module.purchase.specification.PurchaseOrderSpecification;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseOrderHeaderService {

    @Autowired
    private PurchaseOrderHeaderRepository purchaseOrderHeaderRepository;

    @Autowired
    private PurchaseOrderLineService purchaseOrderLineService;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Lazy
    @Autowired
    private QuotationService quotationService;

    @Autowired
    private PurchaseRequestLineService purchaseRequestLineService;

    @Autowired
    private AuditLogsService auditLogsService;

    public PurchaseOrderHeader savePurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader) {
        return purchaseOrderHeaderRepository.save(purchaseOrderHeader);
    }

    public PurchaseOrderHeader addPurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader, Employee employee) {

        purchaseOrderHeader = savePurchaseOrderHeader(purchaseOrderHeader);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_ORDER);
        log.setEntityId(purchaseOrderHeader.getPurchaseOrderId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseOrderHeader;
    }

    public PurchaseOrderHeader updatePurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader, Employee employee) {

        purchaseOrderHeader = savePurchaseOrderHeader(purchaseOrderHeader);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_ORDER);
        log.setEntityId(purchaseOrderHeader.getPurchaseOrderId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseOrderHeader;
    }

    public Long countByStatus(Status status) {
        return purchaseOrderHeaderRepository.countByStatus(status);
    }

    public Long countAll()
    {
        return purchaseOrderHeaderRepository.count();
    }

    public Optional<PurchaseOrderHeader> getPurchaseOrderHeaderById(Long id) {
        Optional<PurchaseOrderHeader> existingPurchaseOrderHeader = purchaseOrderHeaderRepository.findById(id);
        if (!existingPurchaseOrderHeader.isPresent()) {
            throw new ResourceNotFoundException("Purchase order header not found with id: " + id);
        }
        return existingPurchaseOrderHeader;
    }

    public List<PurchaseOrderDTO> getAllPurchaseOrdersfilter(PurchaseOrderDTO purchaseOrderDTO) {
        Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasCreatedBy(purchaseOrderDTO.getCreatedBy()))
                .and(PurchaseOrderSpecification.hasCreatedBy(purchaseOrderDTO.getCreatedBy()))
                .and(PurchaseOrderSpecification.hasStatus(purchaseOrderDTO.getStatus()));

        return purchaseOrderMapper.toPurchaseOrdersDTO(purchaseOrderHeaderRepository.findAll(spec));
    }

    public List<PurchaseOrderDTO> getRecentPurchaseOrders(PageRequest pageRequest) {
        return purchaseOrderMapper.toPurchaseOrdersDTO(purchaseOrderHeaderRepository.findAllByOrderByPurchaseOrderIdDesc(pageRequest));
    }

    public Page<PurchaseOrderDTO> getAllPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO, int page, int size) {

        Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasStatus(purchaseOrderDTO.getStatus()))
                .and(PurchaseOrderSpecification.hasVendor(purchaseOrderDTO.getVendor()));

        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrderHeader> prpage = purchaseOrderHeaderRepository.findAll(spec, pageable);
        return prpage.map(purchaseOrderMapper::toPurchaseOrderDTO);
    }

    public void genratePurchaseOrder(Quotation quotation) {
        PurchaseOrderHeader purchaseOrderHeader = new PurchaseOrderHeader();
        purchaseOrderHeader.setQuotation(quotation);
        purchaseOrderHeader.setStatus(Status.DRAFT);
        purchaseOrderHeader.setCreatedDate(LocalDate.now());
        purchaseOrderHeader.setVendor(quotation.getVendor());
        purchaseOrderHeader.setCreatedBy(null);
        purchaseOrderHeader.setLevel(0);
        purchaseOrderHeader.setTotalAmount(1.0);

        List<PurchaseOrderLine> polines = new ArrayList<>();

        purchaseOrderHeader = savePurchaseOrderHeader(purchaseOrderHeader);
        double aggregatePurchaseOrderGrossValue = 0.0;

        List<QuotationLine> lines = quotationService.getLinesByQuotation(quotation);

        for (QuotationLine line : lines) {
            PurchaseOrderLine poline = new PurchaseOrderLine();
            poline.setItemVariant(line.getItemVariant());
            poline.setPurchaseOrderHeader(purchaseOrderHeader);
            poline.setUnitPrice(line.getUnitPrice());

            double quantity = (line.getRequestForQuotationLine() != null)
                    ? line.getRequestForQuotationLine().getRequestedQuantity()
                    : 0.0;
            poline.setQuantity(quantity);

            double matchedDiscountPercentage = 0.0;
            List<DiscountType> availableSlabs = quotationService.getDiscountsByLine(line);

            for (DiscountType discount : availableSlabs) {
                boolean matchesLowerBound = quantity >= discount.getFromQuantity();
                boolean matchesUpperBound = (discount.getToQuantity() == null) || (quantity <= discount.getToQuantity());

                if (matchesLowerBound && matchesUpperBound) {
                    matchedDiscountPercentage = discount.getDiscountPercentage();
                    break;
                }
            }

            double baseLineGrossCost = poline.getUnitPrice() * poline.getQuantity();
            double finalCalculatedDiscountValue = baseLineGrossCost * (matchedDiscountPercentage / 100.0);
            double accurateLineNetTotal = baseLineGrossCost - finalCalculatedDiscountValue;

            poline.setDiscountAmount(finalCalculatedDiscountValue);
            poline.setTotalAmount(accurateLineNetTotal);

            polines.add(purchaseOrderLineService.addPurchaseOrderLine(poline));

            aggregatePurchaseOrderGrossValue += accurateLineNetTotal;
        }

        purchaseOrderHeader.setTotalAmount(aggregatePurchaseOrderGrossValue);
        purchaseOrderHeader = addPurchaseOrderHeader(purchaseOrderHeader, null);

        List<PurchaseRequestLine> prLines = purchaseRequestLineService.getRequestForQuotation(quotation.getRequestForQuotation());

        for (PurchaseRequestLine line : prLines) {
            for (PurchaseOrderLine poline : polines) {
                if (poline.getItemVariant().equals(line.getItemVariant())) {
                    line.setPurchaseOrderLine(poline);
                    purchaseRequestLineService.updatePurchaseRequestLine(line, null);
                }
                System.out.println(line.getId() + " " + poline.getId());
            }
        }

    }

    public List<PurchaseOrderHeader> getPurchaseOrderList(PurchaseOrderDTO purchaseOrderDTO) {

        Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasStatus(purchaseOrderDTO.getStatus()))
                .and(PurchaseOrderSpecification.hasCreatedBy(purchaseOrderDTO.getCreatedBy()))
                .and(PurchaseOrderSpecification.hasVendor(purchaseOrderDTO.getVendor()));

        return purchaseOrderHeaderRepository.findAll(spec);
    }

    public Long getCountPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO) {

        Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasStatus(purchaseOrderDTO.getStatus()))
                .and(PurchaseOrderSpecification.hasCreatedBy(purchaseOrderDTO.getCreatedBy()))
                .and(PurchaseOrderSpecification.hasVendor(purchaseOrderDTO.getVendor()));

        return purchaseOrderHeaderRepository.count(spec);
    }

}
