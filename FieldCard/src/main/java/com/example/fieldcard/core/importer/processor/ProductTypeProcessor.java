package com.example.fieldcard.core.importer.processor;

import com.example.fieldcard.data.entity.ProductType;
import com.example.fieldcard.data.repository.ProductTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Order(1)
@Component
public class ProductTypeProcessor extends AbstractCsvProcessor {

    private final ProductTypeRepository repository;
    private Map<String, ProductType> existingTypesMap;
    private List<ProductType> typesToSaveOrUpdate;

    @Autowired
    public ProductTypeProcessor(ProductTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik rodzajów preparatu".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(String fileContent) {
        System.out.println("    [ProductTypeProcessor] Rozpoczynam SYNCHRONIZACJĘ 'słownik rodzajów preparatu'...");

        this.typesToSaveOrUpdate = new ArrayList<>();
        List<ProductType> existingTypes = repository.findAll();
        this.existingTypesMap = existingTypes.stream()
                .collect(Collectors.toMap(ProductType::getName, Function.identity()));

        super.process(fileContent);

        int deactivatedCount = 0;
        if (!existingTypesMap.isEmpty()) {
            for (ProductType typeToDeactivate : existingTypesMap.values()) {
                if (typeToDeactivate.isActive()) {
                    typeToDeactivate.setActive(false);
                    this.typesToSaveOrUpdate.add(typeToDeactivate);
                    deactivatedCount++;
                }
            }
        }

        if (!this.typesToSaveOrUpdate.isEmpty()) {
            repository.saveAll(this.typesToSaveOrUpdate);
        }

        System.out.println("    [ProductTypeProcessor] Synchronizacja zakończona.");
        System.out.println("    [ProductTypeProcessor] Przetworzono (zapis/aktualizacja): " + typesToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [ProductTypeProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    @Override
    protected void processRow(String[] record) {
        if (record == null || record.length < 1) {
            return;
        }

        String name = record[0];

        if (name == null || name.trim().isEmpty() || "nazwa".equalsIgnoreCase(name)) {
            return;
        }

        name = name.trim();

        ProductType existingType = this.existingTypesMap.remove(name);

        if (existingType == null) {
            ProductType newType = new ProductType(name);
            newType.setActive(true);
            this.typesToSaveOrUpdate.add(newType);
        } else {
            if (!existingType.isActive()) {
                existingType.setActive(true);
                this.typesToSaveOrUpdate.add(existingType);
            }
        }
    }
}