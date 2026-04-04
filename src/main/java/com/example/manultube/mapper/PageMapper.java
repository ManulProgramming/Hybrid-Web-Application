package com.example.manultube.mapper;


import com.example.manultube.model.Page;

import java.util.function.Function;

public class PageMapper {

    public static <T, R> Page<R> toCustomPage(
            org.springframework.data.domain.Page<T> springPage,
            Function<T, R> mapper
    ) {
        Page<R> page = new Page<>();

        page.setContent(
                springPage.getContent()
                        .stream()
                        .map(mapper)
                        .toList()
        );

        page.setPage(springPage.getNumber() + 1);
        page.setSize(springPage.getSize());
        page.setTotalElements(springPage.getTotalElements());
        page.setTotalPages();

        return page;
    }
}