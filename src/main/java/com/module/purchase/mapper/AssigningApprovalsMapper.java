package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AssigningApprovalsMapper {

    AssigningApprovalsDTO toAssigningApprovalsDTO(AssigningApprovals assigningApprovals);

    @IterableMapping(elementTargetType = AssigningApprovalsDTO.class)
    List<AssigningApprovalsDTO> toAssigningApprovalsDTOList(List<AssigningApprovals> assigningApprovals);
}