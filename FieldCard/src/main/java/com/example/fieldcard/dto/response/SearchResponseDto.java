package com.example.fieldcard.dto.response;

import java.util.ArrayList;
import java.util.List;

public class SearchResponseDto {
    private List<ProductSearchResultDto> results = new ArrayList<>();
    private List<ProductSearchResultDto> suggestions = new ArrayList<>();
    private int totalPages;
    private long totalElements;
    private int currentPage;
    public SearchResponseDto() {
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }
    public  void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public List<ProductSearchResultDto> getResults() {
        return results;
    }

    public void setResults(List<ProductSearchResultDto> results) {
        this.results = results;
    }

    public List<ProductSearchResultDto> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<ProductSearchResultDto> suggestions) {
        this.suggestions = suggestions;
    }
}
