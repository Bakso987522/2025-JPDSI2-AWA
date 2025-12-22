package com.example.fieldcard.api;

import com.example.fieldcard.core.searcher.service.ProductSearchService;
import com.example.fieldcard.dto.request.SearchCriteriaDto;
import com.example.fieldcard.dto.response.ProductDetailsDto;
import com.example.fieldcard.dto.response.SearchResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class SearchController {

    private final ProductSearchService searchService;

    public SearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public SearchResponseDto search(@ModelAttribute SearchCriteriaDto criteria, @PageableDefault(size = 30) Pageable pageable) {
        return searchService.search(criteria, pageable);
    }



    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDto> getProductDetails(@PathVariable Long id) {
        return ResponseEntity.ok(searchService.getProductDetails(id));
    }
}