package com.module.purchase.entity;

import org.hibernate.annotations.ColumnDefault;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(
    name = "request_for_quotation_line",
    uniqueConstraints = @UniqueConstraint(columnNames = {"request_for_quotation_id", "variant_id"})
)
public class RequestForQuotationLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_for_quotation_line_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_for_quotation_id", referencedColumnName = "request_for_quotation_id", nullable = false)
    private RequestForQuotation requestForQuotation;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "variant_id", referencedColumnName = "variant_id", nullable = false)
    private ItemVariant itemVariant;

    @Column(name = "requested_quantity", nullable = false)
    @Positive
    @ColumnDefault("1.0")
    private Double requestedQuantity=1.0;

    @OneToMany(mappedBy ="requestForQuotationLine")
    private List<QuotationLine> quotations;

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