package com.example.fieldcard.api;

import com.example.fieldcard.core.searcher.service.ProductSearchService;
import com.example.fieldcard.dto.request.SearchCriteriaDto;
import com.example.fieldcard.dto.response.SearchResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class SearchController {

    private final ProductSearchService searchService;

    public SearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }
    @GetMapping("/search")
    public SearchResponseDto search(@ModelAttribute SearchCriteriaDto criteria) {
        return searchService.search(criteria);
    }
}