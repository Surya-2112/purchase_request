package com.module.purchase.entity;

import org.hibernate.annotations.ColumnDefault;
import java.util.Set; 
import jakarta.persistence.*;

@Entity
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "specification", length = 1000)
    private String specification;

    @Column(name = "active", nullable = false)
    @ColumnDefault("true")
    private Boolean active = true;

    @Column(name = "estimated_unit_price")
    private Double estimatedUnitPrice;

    @OneToMany(mappedBy = "itemVariant")
    private Set<RequestForQuotationLine> requestForQuotationLines;

    @OneToMany(mappedBy = "itemVariant")
    private Set<QuotationLine> quotationLines;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Double getEstimatedUnitPrice() {
        return estimatedUnitPrice;
    }

    public void setEstimatedUnitPrice(Double estimatedUnitPrice) {
        this.estimatedUnitPrice = estimatedUnitPrice;
    }

    public Set<RequestForQuotationLine> getRequestForQuotationLines() {
        return requestForQuotationLines;
    }

    public void setRequestForQuotationLines(Set<RequestForQuotationLine> requestForQuotationLines) {
        this.requestForQuotationLines = requestForQuotationLines;
    }

    public Set<QuotationLine> getQuotationLines() {
        return quotationLines;
    }

    public void setQuotationLines(Set<QuotationLine> quotationLines) {
        this.quotationLines = quotationLines;
    }
}