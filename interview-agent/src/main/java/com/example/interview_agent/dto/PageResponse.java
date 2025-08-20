package com.example.interview_agent.dto;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Objects;


public class PageResponse<T> {


    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
    private boolean isFirst;

    public PageResponse() {
    }

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.isLast = page.isLast();
        this.isFirst = page.isFirst();
    }


    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() { // 对于boolean类型，getter通常是 is...()
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public boolean isFirst() { // 对于boolean类型，getter通常是 is...()
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }


    @Override
    public String toString() {
        return "PageResponse{" +
                "content.size=" + (content != null ? content.size() : 0) +
                ", currentPage=" + currentPage +
                ", totalPages=" + totalPages +
                '}';
    }
}