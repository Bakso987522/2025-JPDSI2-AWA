package com.example.fieldcard.core.searcher.service;

import com.example.fieldcard.data.entity.PlantProtectionProduct;
import com.example.fieldcard.data.repository.PlantProtectionProductRepository;
import com.example.fieldcard.data.specification.ProductSpecification;
import com.example.fieldcard.dto.request.SearchCriteriaDto;
import com.example.fieldcard.dto.response.ProductSearchResultDto;
import com.example.fieldcard.dto.response.SearchResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    private final PlantProtectionProductRepository productRepository;

    public ProductSearchService(PlantProtectionProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Transactional(readOnly = true)
    public SearchResponseDto search(SearchCriteriaDto criteria) {
        SearchResponseDto response = new SearchResponseDto();

        ProductSpecification spec = new ProductSpecification(criteria);
        List<PlantProtectionProduct> exactMatches = productRepository.findAll(spec);

        if (!exactMatches.isEmpty()) {
            response.setResults(mapToDtos(exactMatches));
        } else {
            if (StringUtils.hasText(criteria.getQuery())) {
                List<PlantProtectionProduct> suggestions = productRepository.findTop5SimilarProducts(criteria.getQuery());
                response.setSuggestions(mapToDtos(suggestions));
            }
        }

        return response;
    }

    private List<ProductSearchResultDto> mapToDtos(List<PlantProtectionProduct> entities) {
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductSearchResultDto mapToDto(PlantProtectionProduct entity) {
        ProductSearchResultDto dto = new ProductSearchResultDto();
        dto.setSorId(entity.getSorId());
        dto.setName(entity.getName());
        dto.setManufacturer(entity.getManufacturer());

        String substances = entity.getActiveSubstances().stream()
                .map(link -> link.getActiveSubstance().getName())
                .collect(Collectors.joining(", "));

        dto.setActiveSubstance(substances);

        return dto;
    }
}