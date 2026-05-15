package com.module.purchase.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableUtil {

    public static Pageable getPageable( PageRequestDTO request ) {

        Sort sort = request.getSortDirection()
                .equalsIgnoreCase("desc")
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );
    }
}