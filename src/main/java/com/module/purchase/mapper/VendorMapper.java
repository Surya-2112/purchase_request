package com.module.purchase.mapper;

import java.util.List;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.VendorDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface VendorMapper {
    
    VendorDTO toVendorDTO(Vendor vendor);

    List<VendorDTO> toVendorDTOList(List<Vendor> vendors);
}
