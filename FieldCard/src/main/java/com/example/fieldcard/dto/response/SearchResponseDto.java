package com.example.fieldcard.dto.response;

import java.util.ArrayList;
import java.util.List;

public class SearchResponseDto {
    private List<ProductSearchResultDto> results = new ArrayList<>();
    private List<ProductSearchResultDto> suggestions = new ArrayList<>();
    public SearchResponseDto() {
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
