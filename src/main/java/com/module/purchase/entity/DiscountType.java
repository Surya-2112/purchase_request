package com.module.purchase.entity;

import org.hibernate.annotations.ColumnDefault;

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
    @JoinColumn(name = "quotation_line_id", referencedColumnName = "quotation_line_id", nullable = false)
    private QuotationLine quotationLine;

    @Column(name = "from_quantity", nullable = false)
    @NotNull
    @Positive
    private Double fromQuantity;

    @Column(name = "to_quantity", nullable = false)
    @Positive
    @NotNull
    private Double toQuantity;

    @Column(name = "discount_percentage", nullable = false)
    @PositiveOrZero
    @NotNull
    @ColumnDefault("0.0")
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