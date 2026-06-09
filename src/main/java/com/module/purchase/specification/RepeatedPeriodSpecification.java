package com.module.purchase.specification;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.FrequencyType;
import com.module.purchase.enums.RepeatedPeriodReferType;

public class RepeatedPeriodSpecification {

    // Filter by primary Key ID
    public static Specification<RepeatedPeriod> hasId(Long id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    // Filter by Reference Module Type (e.g., PURCHASE_REQUEST_LINE)
    public static Specification<RepeatedPeriod> hasReferType(RepeatedPeriodReferType referType) {
        return (root, query, criteriaBuilder) -> {
            if (referType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("referType"), referType);
        };
    }

    // Filter by target Entity Source Row ID
    public static Specification<RepeatedPeriod> hasReferId(Long referId) {
        return (root, query, criteriaBuilder) -> {
            if (referId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("referId"), referId);
        };
    }

    // Filter by Frequency Basis Type (e.g., DAYS, WEEKS, MONTHS)
    public static Specification<RepeatedPeriod> hasFrequencyType(FrequencyType frequencyType) {
        return (root, query, criteriaBuilder) -> {
            if (frequencyType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("frequencyType"), frequencyType);
        };
    }

    // Filter by Exact Next Execution Scheduled Target Run Date
    public static Specification<RepeatedPeriod> hasNextDate(LocalDate nextDate) {
        return (root, query, criteriaBuilder) -> {
            if (nextDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("nextDate"), nextDate);
        };
    }
}