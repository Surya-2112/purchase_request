package com.module.purchase.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.RepeatedPeriodReferType;

@Repository
public interface RepeatedPeriodRepository extends JpaRepository<RepeatedPeriod, Long>, JpaSpecificationExecutor<RepeatedPeriod> {
    
    Optional<RepeatedPeriod> findByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId);

    void deleteByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId);

    @Query("SELECT t FROM RepeatedPeriod t WHERE t.nextDate <= :today")
    List<RepeatedPeriod> findPendingTasks(@Param("today") LocalDate today);
}