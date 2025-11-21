package com.example.fieldcard.api;

import com.example.fieldcard.data.repository.ActiveSubstanceRepository;
import com.example.fieldcard.data.repository.CropRepository;
import com.example.fieldcard.data.repository.PestRepository;
import com.example.fieldcard.data.repository.ProductTypeRepository;
import com.example.fieldcard.dto.response.AutocompleteDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dictionaries")
public class DictionaryController {
    private final CropRepository cropRepository;
    private final PestRepository pestRepository;
    private final ActiveSubstanceRepository activeSubstanceRepository;
    private final ProductTypeRepository productTypeRepository;
    public DictionaryController(CropRepository cropRepository, PestRepository pestRepository, ActiveSubstanceRepository activeSubstanceRepository, ProductTypeRepository productTypeRepository) {
        this.cropRepository = cropRepository;
        this.pestRepository = pestRepository;
        this.activeSubstanceRepository = activeSubstanceRepository;
        this.productTypeRepository = productTypeRepository;
    }

    @GetMapping("/crops")
    public List<AutocompleteDto> getCrops(@RequestParam String query) {
        return cropRepository.findSmartSuggestions(query)
                .stream()
                .map(p -> new AutocompleteDto(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/pests")
    public List<AutocompleteDto> getPests(@RequestParam String query) {
        return pestRepository.findSmartSuggestions(query)
                .stream()
                .map(p -> new AutocompleteDto(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/substances")
    public List<AutocompleteDto> getActiveSubstances(@RequestParam String query) {
        return activeSubstanceRepository.findSmartSuggestions(query)
                .stream()
                .map(p -> new AutocompleteDto(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/types")
    public List<AutocompleteDto> getProductTypes(@RequestParam String query) {
        return productTypeRepository.findSmartSuggestions(  query)
                .stream()
                .map(p -> new AutocompleteDto(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }
}
