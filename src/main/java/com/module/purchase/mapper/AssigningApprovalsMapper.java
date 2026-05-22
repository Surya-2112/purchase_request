package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;

@Mapper(componentModel = "spring")
public interface AssigningApprovalsMapper {

    AssigningApprovalsDTO toAssigningApprovalsDTO(AssigningApprovals assigningApprovals);

   // AssigningApprovals toAssigningApprovaHs(AssigningApprovalsDTO assigningApprovalsDTO);

    List<AssigningApprovalsDTO> toAssigningApprovalsDTOList(List<AssigningApprovals> assigningApprovals);
}