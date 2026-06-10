package com.module.purchase.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"quotation_id", "variant_id"})
    }
)
public class QuotationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_line_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant itemVariant;

    @NotNull
    @Positive
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @OneToMany(mappedBy = "quotationLine",fetch = FetchType.EAGER)
    private Set<DiscountType> discountTypes;

    @ManyToOne
    @JoinColumn(name = "request_for_quotation_line_id")
    private RequestForQuotationLine requestForQuotationLine;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
    }

    public ItemVariant getItemVariant() {
        return itemVariant;
    }

    public void setItemVariant(ItemVariant itemVariant) {
        this.itemVariant = itemVariant;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Set<DiscountType> getDiscountTypes() {
        return discountTypes;
    }

    public void setDiscountTypes(Set<DiscountType> discountTypes) {
        this.discountTypes = discountTypes;
    }

    public RequestForQuotationLine getRequestForQuotationLine() {
        return requestForQuotationLine;
    }

    public void setRequestForQuotationLine(RequestForQuotationLine requestForQuotationLine) {
        this.requestForQuotationLine = requestForQuotationLine;
    }

    
}