package com.module.purchase.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.RepeatedPeriodReferType;

@Repository
public interface RepeatedPeriodRepository extends JpaRepository<RepeatedPeriod, Long> {
    
    Optional<RepeatedPeriod> findByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId);
    List<RepeatedPeriod> findAllByNextDate(LocalDate nextDate);
    void deleteByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId);
}