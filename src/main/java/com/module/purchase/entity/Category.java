package com.module.purchase.entity;

import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(
    uniqueConstraints = {@UniqueConstraint(columnNames = "category_name")
    }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

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
}