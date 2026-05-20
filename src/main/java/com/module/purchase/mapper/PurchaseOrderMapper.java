package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entityDTO.PurchaseOrderDTO;


@Mapper(componentModel="spring")
public interface PurchaseOrderMapper {
    
    PurchaseOrderDTO toPurchaseOrderDTO(PurchaseOrderHeader purchaseRequest);

    List<PurchaseOrderDTO> toPurchaseOrdersDTO(List<PurchaseOrderHeader> purchaseRequests);
}
