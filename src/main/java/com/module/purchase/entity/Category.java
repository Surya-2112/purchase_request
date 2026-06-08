package com.module.purchase.entity;

import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

    @Column(name = "is_repeatable", nullable = false)
    private Boolean repeatable = false;

    // Added to check if automation should instantly send out RFQs to matched vendors
    @Column(name = "auto_rfq", nullable = false)
    private Boolean autoRfq = false;

    @OneToMany(mappedBy = "category")
    private Set<Item> items;

    @ManyToMany(mappedBy = "categories")
    private Set<Vendor> vendors;
 
    public Long getCategoryId() {
        return categoryId;
    } 

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public Set<Vendor> getVendors() {
        return vendors;
    }

    public void setVendors(Set<Vendor> vendors) {
        this.vendors = vendors;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(Boolean repeatable) {
        this.repeatable = repeatable;
    }

    public Boolean isAutoRfq() {
        return autoRfq;
    }

    public void setAutoRfq(Boolean autoRfq) {
        this.autoRfq = autoRfq;
    }

    @Override
    public String toString() {
        return "Category [categoryId=" + categoryId + ", categoryName=" + categoryName + ", repeatable=" + repeatable
                + ", autoRfq=" + autoRfq + "]";
    }

    
}