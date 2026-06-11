package com.module.purchase.entity;

import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table( 
     name = "purchase_order_line",
    uniqueConstraints = {
       @UniqueConstraint(columnNames = {"purchase_order_id", "variant_id"})
    }
)
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_line_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrderHeader purchaseOrderHeader;

    @ManyToOne
    @JoinColumn(name = "variant_id", referencedColumnName = "variant_id", nullable = false)
    private ItemVariant itemVariant;

    @NotNull
    @Column(name = "unit_price", nullable = false)
    @Positive
    private Double unitPrice;

    @NotNull
    @Column(nullable = false)
    @Positive
    private Double quantity;

     @Column(name = "total_amount", nullable = false)
    @Positive
    private Double totalAmount;

    @Column(name = "discount_amount", nullable = false)
    @ColumnDefault("0.0")
    @PositiveOrZero
    private Double discountAmount;

    @OneToMany(mappedBy = "purchaseOrderLine")
    private Set<PurchaseRequestLine> purchaseRequestLines;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseOrderHeader getPurchaseOrderHeader() {
        return purchaseOrderHeader;
    }

    public void setPurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader) {
        this.purchaseOrderHeader = purchaseOrderHeader;
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

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Set<PurchaseRequestLine> getPurchaseRequestLines() {
        return purchaseRequestLines;
    }

    public void setPurchaseRequestLines(Set<PurchaseRequestLine> purchaseRequestLines) {
        this.purchaseRequestLines = purchaseRequestLines;
    }    
}