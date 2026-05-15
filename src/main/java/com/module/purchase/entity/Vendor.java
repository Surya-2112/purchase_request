package com.module.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    private String vendorName;

    private String  vendorEmail;

    private String  vendorPhone;
    
    @Embedded
    private Address vendorAddress;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"vendors"})
    private VendorCategory vendorCategory;

    private Boolean active;

    @OneToMany(mappedBy= "vendor")
    @JsonIgnoreProperties("vendor")
    private List<PurchaseOrderHeader> purchaseOrderHeader;

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public void setVendorPhone(String vendorPhone) {
        this.vendorPhone = vendorPhone;
    }

    public Address getVendorAddress() {
        return vendorAddress;
    }

    public void setVendorAddress(Address vendorAddress) {
        this.vendorAddress = vendorAddress;
    }

    
    public VendorCategory getVendorCategory() {
        return vendorCategory;
    }

    public void setVendorCategory(VendorCategory vendorCategory) {
        this.vendorCategory = vendorCategory;
    }

    public List<PurchaseOrderHeader> getPurchaseOrderHeader() {
        return purchaseOrderHeader;
    }

    public void setPurchaseOrderHeader(List<PurchaseOrderHeader> purchaseOrderHeader) {
        this.purchaseOrderHeader = purchaseOrderHeader;
    }

}
