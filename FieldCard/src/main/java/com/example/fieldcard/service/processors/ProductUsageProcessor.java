package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.Crop;
import com.example.fieldcard.entity.Pest;
import com.example.fieldcard.entity.PlantProtectionProduct;
import com.example.fieldcard.entity.ProductUsage;
import com.example.fieldcard.repository.CropRepository;
import com.example.fieldcard.repository.PestRepository;
import com.example.fieldcard.repository.PlantProtectionProductRepository;
import com.example.fieldcard.repository.ProductUsageRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Order(7)
@Component
public class ProductUsageProcessor implements FileProcessor {

    private final ProductUsageRepository productUsageRepository;
    private final PlantProtectionProductRepository productRepository;
    private final CropRepository cropRepository;
    private final PestRepository pestRepository;

    private Map<String, PlantProtectionProduct> productMapBySorId;
    private Map<String, Crop> cropMapByName;
    private Map<String, Pest> pestMapByName;

    private Map<String, ProductUsage> existingUsagesMap;
    private List<ProductUsage> usagesToSaveOrUpdate;

    @Autowired
    public ProductUsageProcessor(ProductUsageRepository productUsageRepository,
                                 PlantProtectionProductRepository productRepository,
                                 CropRepository cropRepository,
                                 PestRepository pestRepository) {
        this.productUsageRepository = productUsageRepository;
        this.productRepository = productRepository;
        this.cropRepository = cropRepository;
        this.pestRepository = pestRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "rejestr zastosowań".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(byte[] fileContent) {
        System.out.println("    [ProductUsageProcessor] Rozpoczynam SYNCHRONIZACJĘ 'rejestr zastosowań' (Soft Delete)...");

        loadCaches();
        loadExistingUsagesMap();

        this.usagesToSaveOrUpdate = new ArrayList<>();

        try (InputStream is = new ByteArrayInputStream(fileContent);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    processRow(row);
                }
            }
        } catch (Exception e) {
            System.out.println("    [ProductUsageProcessor] Krytyczny błąd podczas parsowania XLSX: " + e.getMessage());
        }

        int deactivatedCount = 0;
        for (ProductUsage usageToDeactivate : this.existingUsagesMap.values()) {
            if (usageToDeactivate.isActive()) {
                usageToDeactivate.setActive(false);
                this.usagesToSaveOrUpdate.add(usageToDeactivate);
                deactivatedCount++;
            }
        }

        if (!usagesToSaveOrUpdate.isEmpty()) {
            productUsageRepository.saveAll(usagesToSaveOrUpdate);
        }

        System.out.println("    [ProductUsageProcessor] Synchronizacja zakończona.");
        System.out.println("    [ProductUsageProcessor] Przetworzono (zapis/aktualizacja): " + usagesToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [ProductUsageProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");

        clearCaches();
    }

    private void loadCaches() {
        System.out.println("    [ProductUsageProcessor] Ładowanie słowników do pamięci...");
        productMapBySorId = productRepository.findAll().stream()
                .collect(Collectors.toMap(PlantProtectionProduct::getSorId, Function.identity()));
        cropMapByName = cropRepository.findAll().stream()
                .collect(Collectors.toMap(Crop::getName, Function.identity()));
        pestMapByName = pestRepository.findAll().stream()
                .collect(Collectors.toMap(Pest::getName, Function.identity()));
    }

    private void loadExistingUsagesMap() {
        System.out.println("    [ProductUsageProcessor] Ładowanie istniejących zastosowań do mapy...");
        List<ProductUsage> allUsages = productUsageRepository.findAllWithRelationships();
        this.existingUsagesMap = allUsages.stream()
                .collect(Collectors.toMap(this::buildUsageKey, Function.identity(), (existing, replacement) -> existing));
        System.out.println("    [ProductUsageProcessor] Znaleziono " + this.existingUsagesMap.size() + " istniejących zastosowań w bazie.");
    }

    private String buildUsageKey(ProductUsage usage) {
        String sorId = usage.getProduct().getSorId();
        String cropName = usage.getCrop().getName();
        String pestName = usage.getPest().getName();
        return sorId + "|" + cropName + "|" + pestName;
    }

    private String buildUsageKey(String sorId, String cropName, String pestName) {
        return sorId + "|" + cropName + "|" + pestName;
    }

    private void clearCaches() {
        productMapBySorId.clear();
        cropMapByName.clear();
        pestMapByName.clear();
        existingUsagesMap.clear();
    }


    private List<String> parseNames(String cellValue) {
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(cellValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    protected void processRow(Row row) {
        try {
            String sorId = getStringCellValue(row.getCell(0));
            String cropNamesStr = getStringCellValue(row.getCell(1));
            String pestNamesStr = getStringCellValue(row.getCell(2));
            String dosage = getStringCellValue(row.getCell(3));
            String applicationTiming = getStringCellValue(row.getCell(4));
            String minorUseStr = getStringCellValue(row.getCell(6));

            PlantProtectionProduct product = productMapBySorId.get(sorId);
            if (product == null) {
                return;
            }

            List<String> cropNames = parseNames(cropNamesStr);
            List<String> pestNames = parseNames(pestNamesStr);

            if (cropNames.isEmpty() || pestNames.isEmpty()) {
                return;
            }

            for (String cropName : cropNames) {
                for (String pestName : pestNames) {

                    Crop crop = cropMapByName.get(cropName);
                    Pest pest = pestMapByName.get(pestName);

                    if (crop == null || pest == null) {
                        continue;
                    }

                    String key = buildUsageKey(sorId, cropName, pestName);

                    ProductUsage existingUsage = this.existingUsagesMap.remove(key);

                    if (existingUsage == null) {
                        ProductUsage newUsage = new ProductUsage();
                        newUsage.setProduct(product);
                        newUsage.setCrop(crop);
                        newUsage.setPest(pest);
                        newUsage.setDosage(dosage);
                        newUsage.setApplicationTiming(applicationTiming);
                        newUsage.setMinorUse("TAK".equalsIgnoreCase(minorUseStr));
                        newUsage.setActive(true);

                        this.usagesToSaveOrUpdate.add(newUsage);
                    } else {
                        boolean needsUpdate = false;
                        if (!existingUsage.isActive()) {
                            existingUsage.setActive(true);
                            needsUpdate = true;
                        }
                        if (!Objects.equals(existingUsage.getDosage(), dosage)) {
                            existingUsage.setDosage(dosage);
                            needsUpdate = true;
                        }
                        if (!Objects.equals(existingUsage.getApplicationTiming(), applicationTiming)) {
                            existingUsage.setApplicationTiming(applicationTiming);
                            needsUpdate = true;
                        }

                        if (needsUpdate) {
                            this.usagesToSaveOrUpdate.add(existingUsage);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("    [ProductUsageProcessor] Błąd wierszu Excela: " + row.getRowNum() + ". Błąd: " + e.getMessage());
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }
}