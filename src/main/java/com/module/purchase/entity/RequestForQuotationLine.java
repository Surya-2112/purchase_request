package com.module.purchase.entity;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"request_for_quotation_id", "variant_id"})
)
public class RequestForQuotationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_for_quotation_line_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_for_quotation_id", nullable = false)
    private RequestForQuotation requestForQuotation;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant itemVariant;

    @Column(name = "requested_quantity", nullable = false)
    @Positive
    @ColumnDefault("1.0")
    private Double requestedQuantity=1.0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestForQuotation getRequestForQuotation() {
        return requestForQuotation;
    }

    public void setRequestForQuotation(RequestForQuotation requestForQuotation) {
        this.requestForQuotation = requestForQuotation;
    }

    public ItemVariant getItemVariant() {
        return itemVariant;
    }

    public void setItemVariant(ItemVariant itemVariant) {
        this.itemVariant = itemVariant;
    }

    public Double getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Double requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

}