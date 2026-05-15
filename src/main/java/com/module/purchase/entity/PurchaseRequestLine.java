package com.module.purchase.entity;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PurchaseRequestLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseRequestLineId;

    @ManyToOne
    @JoinColumn(name = "purchaseRequestId")
    @JsonIgnoreProperties({"purchaseRequestLines"})
    private PurchaseRequestHeader purchaseRequestHeader;

    @ManyToOne
    @JoinColumn(name = "itemId")
    @JsonIgnoreProperties({"purchaseRequestLines"})
    private Item item;

    private Integer quantity;

    private Double unitPrice;

    private Double discount;

    private Double totalPrice;

    public Long getPurchaseRequestLineId() {
        return purchaseRequestLineId;
    }

    public void setPurchaseRequestLineId(Long purchaseRequestLineId) {
        this.purchaseRequestLineId = purchaseRequestLineId;
    }

    public PurchaseRequestHeader getPurchaseRequestHeader() {
        return purchaseRequestHeader;
    }

    public void setPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        this.purchaseRequestHeader = purchaseRequestHeader;
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
