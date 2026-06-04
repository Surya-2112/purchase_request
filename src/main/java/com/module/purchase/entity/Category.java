package com.module.purchase.entity;

import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(
    name = "category",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "category_name")
    }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;

    @OneToOne(mappedBy = "category")
    private Item item;

    @ManyToMany(mappedBy = "categories")
    private Set<Vendor> vendors;
 
    public Long getCategoryId() {
        return categoryId;
    } 

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
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