package com.module.purchase.entity;

import java.time.LocalDate;

import com.module.purchase.enums.FrequencyType;
import com.module.purchase.enums.RepeatedPeriodReferType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "repeated_period")
public class RepeatedPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repeated_period_id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "refer_type", nullable = false)
    private RepeatedPeriodReferType referType;

    @NotNull
    @Column(name = "refer_id", nullable = false)
    private Long referId;

    @NotNull
    @Column(name = "frequency_period", nullable = false)
    private Integer frequencyPeriod;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "frequency_type", nullable = false)
    private FrequencyType frequencyType;

    @NotNull
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "next_date")
    private LocalDate nextDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RepeatedPeriodReferType getReferType() {
        return referType;
    }

    public void setReferType(RepeatedPeriodReferType referType) {
        this.referType = referType;
    }

    public Long getReferId() {
        return referId;
    }

    public void setReferId(Long referId) {
        this.referId = referId;
    }

    public Integer getFrequencyPeriod() {
        return frequencyPeriod;
    }

    public void setFrequencyPeriod(Integer frequencyPeriod) {
        this.frequencyPeriod = frequencyPeriod;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public LocalDate getNextDate() {
        return nextDate;
    }

    public void setNextDate(LocalDate nextDate) {
        this.nextDate = nextDate;
    }


}