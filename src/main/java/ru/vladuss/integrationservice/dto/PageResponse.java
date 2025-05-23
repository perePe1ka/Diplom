package ru.vladuss.integrationservice.dto;

import java.util.List;
import java.util.Objects;

public final class PageResponse<T> {
    private List<T> content;
    private int offset;
    private int limit;
    private int size;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int offset, int limit) {
        this.content = content;
        this.offset = offset;
        this.limit = limit;
        this.size = Objects.requireNonNullElse(content, List.of()).size();
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}