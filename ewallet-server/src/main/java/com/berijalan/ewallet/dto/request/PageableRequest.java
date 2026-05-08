package com.berijalan.ewallet.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface PageableRequest {
    Integer page();
    Integer size();

    default Pageable toPageable(Sort sort) {
        return PageRequest.of(page(), size(), sort);
    }
}
