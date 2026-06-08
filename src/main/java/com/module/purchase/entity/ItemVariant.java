package com.module.purchase.entity;

import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "item_variant")
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column( length = 1000)
    private String specification;

    @Column(nullable = false)
    @NotNull
    @ColumnDefault("true")
    private Boolean active = true;

    private Double estimatedUnitPrice;

    @OneToMany(mappedBy = "itemVariant")
    private Set<RequestForQuotationLine> requestForQuotationLines;

    @OneToMany(mappedBy = "itemVariant")
    private Set<QuotationLine> quotationLines;

    @OneToMany(mappedBy = "itemVariant")
    private Set<PurchaseRequestLine> purchaseRequestLines;

      @OneToMany(mappedBy = "itemVariant")
    private Set<PurchaseOrderLine> purchaseOrderLines;

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

    public Set<PurchaseRequestLine> getPurchaseRequestLines() {
        return purchaseRequestLines;
    }

    public void setPurchaseRequestLines(Set<PurchaseRequestLine> purchaseRequestLines) {
        this.purchaseRequestLines = purchaseRequestLines;
    }

    public Set<PurchaseOrderLine> getPurchaseOrderLines() {
        return purchaseOrderLines;
    }

    public void setPurchaseOrderLines(Set<PurchaseOrderLine> purchaseOrderLines) {
        this.purchaseOrderLines = purchaseOrderLines;
    }
}