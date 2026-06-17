package com.module.purchase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "discount_type")
public class DiscountType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_type_id")
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "quotation_line_id", referencedColumnName = "quotation_line_id")
    private QuotationLine quotationLine;

    @Column(name = "from_quantity")
    @NotNull
    @Positive
    private Double fromQuantity;

    @Column(name = "to_quantity")
    @Positive
    @NotNull
    private Double toQuantity;

    @Column(name = "discount_percentage")
    @PositiveOrZero
    @NotNull
    private Double discountPercentage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuotationLine getQuotationLine() {
        return quotationLine;
    }

    public void setQuotationLine(QuotationLine quotationLine) {
        this.quotationLine = quotationLine;
    }

    public Double getFromQuantity() {
        return fromQuantity;
    }

    public void setFromQuantity(Double fromQuantity) {
        this.fromQuantity = fromQuantity;
    }

    public Double getToQuantity() {
        return toQuantity;
    }

    public void setToQuantity(Double toQuantity) {
        this.toQuantity = toQuantity;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
}