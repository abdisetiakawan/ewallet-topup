package com.berijalan.ewallet.mapper;

import java.util.List;

public interface BaseMapper<S, T> {

    T toDto(S source);

    default List<T> toDtos(List<S> sources) {
        return sources.stream()
                .map(this::toDto)
                .toList();
    }
}
