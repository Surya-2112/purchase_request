package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.module.purchase.entity.Quotation;
import com.module.purchase.entityDTO.QuotationDTO;

@Mapper(componentModel="spring")
public interface QuotationMapper {
    
    QuotationDTO toQuotationDTO(Quotation quotation);

    List<QuotationDTO> toQuotationDTO(List<Quotation> quotations);

}
