package com.module.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PurchaseOrderLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderLineId;

    @ManyToOne
    @JoinColumn(name = "purchaseOrderId")
    @JsonIgnoreProperties({"purchaseOrderLines"})
    private PurchaseOrderHeader purchaseOrderHeader;

    @ManyToOne
    @JoinColumn(name = "itemId")
    @JsonIgnoreProperties({"purchaseOrderLines"})
    private Item item;

    private Integer quantity;

    private Double unitPrice;

    private Double discount;

    private Double totalPrice;

    public Long getPurchaseOrderLineId() {
        return purchaseOrderLineId;
    }

    public void setPurchaseOrderLineId(Long purchaseOrderLineId) {
        this.purchaseOrderLineId = purchaseOrderLineId;
    }

    public PurchaseOrderHeader getPurchaseOrderHeader() {
        return purchaseOrderHeader;
    }

    public void setPurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader) {
        this.purchaseOrderHeader = purchaseOrderHeader;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    

}
