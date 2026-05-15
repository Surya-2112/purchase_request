package com.module.purchase.mapper;

import org.mapstruct.Mapper;
import java.util.List;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entityDTO.PurchaseRequestDTO;

@Mapper(componentModel="spring")
public interface PurchaseRequestMapper {
    
    PurchaseRequestDTO toPurchaseRequestDTO(PurchaseRequestHeader purchaseRequest);

    List<PurchaseRequestDTO> toPurchaseRequestDTO(List<PurchaseRequestHeader> purchaseRequests);

}
