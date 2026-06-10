package com.module.purchase.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Table;

@Entity
@Table(name = "vendor")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Long vendorId;

    @NotNull
    @Size(max = 100)
    @Column(name = "vendor_name", nullable = false, unique = true, length = 100)
    private String vendorName;

    @NotNull
     @Column(name = "vendor_email", nullable = false, unique = true)
    @Email
    private String vendorEmail;

    @Size(min = 10, max = 15)
     @Column(name = "vendor_phone_number", length = 15)
    private String vendorPhoneNumber;

    @Embedded
    private Address vendorAddress;

    @OneToOne
     @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Users users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vendor_categories", 
            joinColumns = @JoinColumn(name = "vendor_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;

    @OneToMany(mappedBy = "vendor")
    @JsonIgnoreProperties("vendor")
    private Set<Quotation> quotations;

    @NotNull
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

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public Set<Quotation> getQuotations() {
        return quotations;
    }

    public void setQuotations(Set<Quotation> quotations) {
        this.quotations = quotations;
    }

    @Override
    public String toString() {
        return "Vendor [vendorId=" + vendorId + ", vendorName=" + vendorName + ", vendorEmail=" + vendorEmail
                + ", vendorPhoneNumber=" + vendorPhoneNumber + ", vendorAddress=" + vendorAddress + ", active=" + active
                + "]";
    }
}
