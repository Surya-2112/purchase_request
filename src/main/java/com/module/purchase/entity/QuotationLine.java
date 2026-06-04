package com.module.purchase.entity;

import java.util.Set;

import jakarta.persistence.*;

@Entity
public class QuotationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_line_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant itemVariant;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @OneToMany(mappedBy = "quotationLine")
    private Set<DiscountType> discountTypes;

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

    
}