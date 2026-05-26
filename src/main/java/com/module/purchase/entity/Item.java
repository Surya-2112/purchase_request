package com.module.purchase.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;

@Entity
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    private String itemName;

    @Size(max = 4)
    private String itemCode;

    private Double unitPrice;

    private String VATCode;

    @OneToMany(mappedBy = "item")
    @JsonIgnoreProperties({"item"})
    private List<PurchaseRequestLine> purchaseRequestLines;

    @OneToMany(mappedBy = "item")
    @JsonIgnoreProperties({"item"})
    private List<PurchaseOrderLine> purchaseOrderLines;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public List<PurchaseRequestLine> getPurchaseRequestLines() {
        return purchaseRequestLines;
    }

    public void setPurchaseRequestLines(List<PurchaseRequestLine> purchaseRequestLines) {
        this.purchaseRequestLines = purchaseRequestLines;
    }

    public List<PurchaseOrderLine> getPurchaseOrderLines() {
        return purchaseOrderLines;
    }

    public void setPurchaseOrderLines(List<PurchaseOrderLine> purchaseOrderLines) {
        this.purchaseOrderLines = purchaseOrderLines;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getVATCode() {
        return VATCode;
    }

    public void setVATCode(String VATCode) {
        
        this.VATCode = VATCode;
    }

}
