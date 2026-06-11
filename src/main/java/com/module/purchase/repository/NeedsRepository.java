package com.module.purchase.repository;

import java.util.List;
import com.module.purchase.entity.Needs;
import com.module.purchase.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeedsRepository extends JpaRepository<Needs, Integer> {

    List<Needs> findByEntityType(EntityType entityType);


    List<Needs> findByEntityTypeAndRefId(EntityType entityType, Long refId);

    void deleteByEntityTypeAndId(EntityType entityType, Long Id);
}