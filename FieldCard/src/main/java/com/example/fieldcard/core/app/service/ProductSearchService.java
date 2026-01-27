package com.example.fieldcard.core.searcher.service;

import com.example.fieldcard.data.entity.PlantProtectionProduct;
import com.example.fieldcard.data.repository.PlantProtectionProductRepository;
import com.example.fieldcard.data.specification.ProductSpecification;
import com.example.fieldcard.dto.request.SearchCriteriaDto;
import com.example.fieldcard.dto.response.ProductDetailsDto;
import com.example.fieldcard.dto.response.ProductSearchResultDto;
import com.example.fieldcard.dto.response.ProductUsageDto;
import com.example.fieldcard.dto.response.SearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    private final PlantProtectionProductRepository productRepository;

    public ProductSearchService(PlantProtectionProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public SearchResponseDto search(SearchCriteriaDto criteria, Pageable pageable) {
        SearchResponseDto response = new SearchResponseDto();

        ProductSpecification spec = new ProductSpecification(criteria);
        Page<PlantProtectionProduct> exactMatches = productRepository.findAll(spec, pageable);

        if (!exactMatches.isEmpty()) {
            response.setResults(mapToDtos(exactMatches.getContent()));
            response.setTotalPages(exactMatches.getTotalPages());
            response.setTotalElements(exactMatches.getTotalElements());
            response.setCurrentPage(exactMatches.getNumber());
        } else {
            if (StringUtils.hasText(criteria.getQuery())) {
                List<PlantProtectionProduct> suggestions = productRepository.findTop5SimilarProducts(criteria.getQuery());
                response.setSuggestions(mapToDtos(suggestions));
            }
        }

        return response;
    }

    @Transactional(readOnly = true)
    public ProductDetailsDto getProductDetails(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDetailsDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono produktu o ID: " + id));
    }

    private List<ProductSearchResultDto> mapToDtos(List<PlantProtectionProduct> entities) {
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProductSearchResultDto mapToDto(PlantProtectionProduct entity) {
        ProductSearchResultDto dto = new ProductSearchResultDto();

        dto.setId(entity.getId());
        dto.setSorId(entity.getSorId());
        dto.setName(entity.getName());
        dto.setManufacturer(entity.getManufacturer());

        List<String> substances = entity.getActiveSubstances().stream()
                .map(link -> link.getActiveSubstance().getName())
                .collect(Collectors.toList());
        dto.setActiveSubstance(substances);

        if (entity.getProductTypes() != null && !entity.getProductTypes().isEmpty()) {
            List<String> types = entity.getProductTypes().stream()
                    .map(pt -> pt.getName())
                    .collect(Collectors.toList());
            dto.setType(types);
        } else {
            dto.setType(List.of("Inny"));
        }

        if (entity.getUsages() != null && !entity.getUsages().isEmpty()) {
            List<String> distinctCrops = entity.getUsages().stream()
                    .filter(u -> u.getCrop() != null)
                    .map(u -> u.getCrop().getName())
                    .distinct()
                    .sorted()
                    .limit(15)
                    .collect(Collectors.toList());
            dto.setCrops(distinctCrops);

            List<String> distinctPests = entity.getUsages().stream()
                    .filter(u -> u.getPest() != null)
                    .map(u -> u.getPest().getName())
                    .distinct()
                    .sorted()
                    .limit(15)
                    .collect(Collectors.toList());
            dto.setPests(distinctPests);
        } else {
            dto.setCrops(Collections.emptyList());
            dto.setPests(Collections.emptyList());
        }

        return dto;
    }

    private ProductDetailsDto mapToDetailsDto(PlantProtectionProduct entity) {
        ProductDetailsDto dto = new ProductDetailsDto();

        dto.setId(entity.getId());
        dto.setSorId(entity.getSorId());
        dto.setName(entity.getName());
        dto.setManufacturer(entity.getManufacturer());
        dto.setPermitNumber(entity.getPermitNumber());
        dto.setSalesDeadline(entity.getSalesDeadline());
        dto.setUseDeadline(entity.getUseDeadline());
        dto.setLabelUrl(entity.getLabelUrl());

        if (entity.getProductTypes() != null && !entity.getProductTypes().isEmpty()) {
            List<String> types = entity.getProductTypes().stream()
                    .map(pt -> pt.getName())
                    .collect(Collectors.toList());
            dto.setType(types);
        } else {
            dto.setType(List.of("Inny"));
        }

        List<String> substances = entity.getActiveSubstances().stream()
                .map(pas -> pas.getActiveSubstance().getName() + " (" + pas.getContent() + ")")
                .collect(Collectors.toList());
        dto.setActiveSubstances(substances);

        if (entity.getUsages() != null && !entity.getUsages().isEmpty()) {
            List<ProductUsageDto> usages = entity.getUsages().stream()
                    .map(usage -> new ProductUsageDto(
                            usage.getCrop() != null ? usage.getCrop().getName() : "Nieznana uprawa",
                            usage.getPest() != null ? usage.getPest().getName() : "Nieznany agrofag",
                            usage.getDosage()
                    ))
                    .collect(Collectors.toList());
            dto.setUsages(usages);

            List<String> distinctCrops = entity.getUsages().stream()
                    .filter(u -> u.getCrop() != null)
                    .map(u -> u.getCrop().getName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            dto.setCrops(distinctCrops);

            List<String> distinctPests = entity.getUsages().stream()
                    .filter(u -> u.getPest() != null)
                    .map(u -> u.getPest().getName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            dto.setPests(distinctPests);
        } else {
            dto.setUsages(Collections.emptyList());
            dto.setCrops(Collections.emptyList());
            dto.setPests(Collections.emptyList());
        }

        return dto;
    }
}