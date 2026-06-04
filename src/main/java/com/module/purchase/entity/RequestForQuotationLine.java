package com.module.purchase.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "request_for_quotation_line")
public class RequestForQuotationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_for_quotation_line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_for_quotation_id", nullable = false)
    private RequestForQuotation requestForQuotation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant itemVariant;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "requested_quantity", nullable = false)
    private Double requestedQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestForQuotation getRequestForQuotation() {
        return requestForQuotation;
    }

    public void setRequestForQuotation(RequestForQuotation requestForQuotation) {
        this.requestForQuotation = requestForQuotation;
    }

    public ItemVariant getItemVariant() {
        return itemVariant;
    }

    public void setItemVariant(ItemVariant itemVariant) {
        this.itemVariant = itemVariant;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Double requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    
}