package com.example.manultube.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class Page<T> {
    @Setter
    private List<T> content;
    @Setter
    private int page;
    @Setter
    private int size;
    @Setter
    private long totalElements;
    private int totalPages;

    public void setTotalPages() {
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }
}