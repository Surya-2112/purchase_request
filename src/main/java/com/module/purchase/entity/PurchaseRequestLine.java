package com.module.purchase.entity;

import com.module.purchase.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(
     name = "purchase_request_line",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"purchase_request_id", "variant_id"})
    }
)
public class PurchaseRequestLine {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_request_line_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_request_id", nullable = false)
    private PurchaseRequestHeader purchaseRequestHeader;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false) 
    private ItemVariant itemVariant;

    @Positive
    @Column(name = "item_unit_price") 
    private Double itemUnitPrice;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Positive
    @Column(name = "requested_quantity", nullable = false)
    private Double requestedQuantity;

    @Column(name = "item_total_amount")
    private Double itemTotalAmount;

    @PositiveOrZero
    @Column(name = "approved_quantity")
    private  Double approvedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.DRAFT;

    @PositiveOrZero
    @Column(name = "ordered_quantity")
    private Double orderedQuantity;

    @Column(name = "repeatable_id", updatable = false)
    private Long repeatableId;

    @ManyToOne
    @JoinColumn(name = "request_for_quotation_id")
    private RequestForQuotation requestForQuotation;

    @ManyToOne
    @JoinColumn(name = "purchase_order_line_id")    
    private PurchaseOrderLine purchaseOrderLine;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseRequestHeader getPurchaseRequestHeader() {
        return purchaseRequestHeader;
    }

    public void setPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        this.purchaseRequestHeader = purchaseRequestHeader;
    }

    public ItemVariant getItemVariant() {
        return itemVariant;
    }

    public void setItemVariant(ItemVariant itemVariant) {
        this.itemVariant = itemVariant;
    }

    public Double getItemUnitPrice() {
        return itemUnitPrice;
    }

    public void setItemUnitPrice(Double itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Double requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Double getItemTotalAmount() {
        return itemTotalAmount;
    }

    public void setItemTotalAmount(Double itemTotalAmount) {
        this.itemTotalAmount = itemTotalAmount;
    }

    public Double getApprovedQuantity() {
        return approvedQuantity;
    }

    public void setApprovedQuantity(Double approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Double getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(Double orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public Long getRepeatableId() {
        return repeatableId;
    }

    public void setRepeatableId(Long repeatableId) {
        this.repeatableId = repeatableId;
    }

    public RequestForQuotation getRequestForQuotation() {
        return requestForQuotation;
    }

    public void setRequestForQuotation(RequestForQuotation requestForQuotation) {
        this.requestForQuotation = requestForQuotation;
    } 

    public PurchaseOrderLine getPurchaseOrderLine() {
        return purchaseOrderLine;
    }

    public void setPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        this.purchaseOrderLine = purchaseOrderLine;
    }
}