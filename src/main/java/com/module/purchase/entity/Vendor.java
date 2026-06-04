package com.module.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;


@Entity
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    @Size(max = 100)
    @Column(nullable = false,length=100)
    private String vendorName;

    @Column(nullable = false, unique = true)
    @Email
    private String vendorEmail;
    
    @Size(min = 10 ,max = 15)
    @Column(length=15)
    private String vendorPhoneNumber;

    @Embedded
    private Address vendorAddress;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToMany
    @JoinTable(name = "vendor_category",  
    joinColumns = @JoinColumn(name = "vendor_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(mappedBy = "vendors")
    private Set<Quotation> quotations;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean active;

    @OneToMany(mappedBy = "vendor")
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

    public String getVendorPhoneNumber() {
        return vendorPhoneNumber;
    }

    public void setVendorPhoneNumber(String vendorPhoneNumber) {
        this.vendorPhoneNumber = vendorPhoneNumber;
    }

    public Address getVendorAddress() {
        return vendorAddress;
    }

    public void setVendorAddress(Address vendorAddress) {
        this.vendorAddress = vendorAddress;
    }
    public List<PurchaseOrderHeader> getPurchaseOrderHeader() {
        return purchaseOrderHeader;
    }

    public void setPurchaseOrderHeader(List<PurchaseOrderHeader> purchaseOrderHeader) {
        this.purchaseOrderHeader = purchaseOrderHeader;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Set<Quotation> getQuotations() {
        return quotations;
    }

    public void setQuotations(Set<Quotation> quotations) {
        this.quotations = quotations;
    }

    

}
