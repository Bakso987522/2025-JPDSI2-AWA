package com.example.fieldcard.core.importer.processor;

import com.example.fieldcard.data.entity.Crop;
import com.example.fieldcard.data.entity.Pest;
import com.example.fieldcard.data.entity.PlantProtectionProduct;
import com.example.fieldcard.data.entity.ProductUsage;
import com.example.fieldcard.data.repository.CropRepository;
import com.example.fieldcard.data.repository.PestRepository;
import com.example.fieldcard.data.repository.PlantProtectionProductRepository;
import com.example.fieldcard.data.repository.ProductUsageRepository;
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
import java.util.*;
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

    private Map<String, List<ProductUsage>> existingUsagesMap;
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
        System.out.println("    [ProductUsageProcessor] Rozpoczynam SYNCHRONIZACJĘ 'rejestr zastosowań'...");

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
            System.out.println("    [ProductUsageProcessor] Krytyczny błąd XLSX: " + e.getMessage());
            e.printStackTrace();
        }

        int deactivatedCount = 0;
        for (List<ProductUsage> remainingUsages : this.existingUsagesMap.values()) {
            for (ProductUsage usageToDeactivate : remainingUsages) {
                if (usageToDeactivate.isActive()) {
                    usageToDeactivate.setActive(false);
                    this.usagesToSaveOrUpdate.add(usageToDeactivate);
                    deactivatedCount++;
                }
            }
        }

        if (!usagesToSaveOrUpdate.isEmpty()) {
            productUsageRepository.saveAll(usagesToSaveOrUpdate);
        }

        System.out.println("    [ProductUsageProcessor] Zakończono.");
        System.out.println("    [ProductUsageProcessor] Zapisano/Zaktualizowano: " + usagesToSaveOrUpdate.size());
        System.out.println("    [ProductUsageProcessor] Wyłączono (soft delete): " + deactivatedCount);

        clearCaches();
    }

    private void loadCaches() {
        productMapBySorId = productRepository.findAll().stream()
                .collect(Collectors.toMap(PlantProtectionProduct::getSorId, Function.identity()));
        cropMapByName = cropRepository.findAll().stream()
                .collect(Collectors.toMap(Crop::getName, Function.identity()));
        pestMapByName = pestRepository.findAll().stream()
                .collect(Collectors.toMap(Pest::getName, Function.identity()));
    }

    private void loadExistingUsagesMap() {
        List<ProductUsage> allUsages = productUsageRepository.findAllWithRelationships();
        this.existingUsagesMap = allUsages.stream()
                .collect(Collectors.groupingBy(
                        this::buildUsageKey,
                        Collectors.toCollection(ArrayList::new)
                ));
    }

    private String buildUsageKey(ProductUsage usage) {
        String sorId = usage.getProduct() != null ? usage.getProduct().getSorId() : "null";
        String cropName = usage.getCrop() != null ? usage.getCrop().getName() : "null";
        String pestName = usage.getPest() != null ? usage.getPest().getName() : "null";
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
            if (product == null) return;

            List<String> cropNames = parseNames(cropNamesStr);
            List<String> pestNames = parseNames(pestNamesStr);

            if (cropNames.isEmpty() || pestNames.isEmpty()) return;

            for (String cropName : cropNames) {
                for (String pestName : pestNames) {

                    Crop crop = cropMapByName.get(cropName);
                    Pest pest = pestMapByName.get(pestName);

                    if (crop == null || pest == null) continue;

                    String key = buildUsageKey(sorId, cropName, pestName);

                    List<ProductUsage> candidates = this.existingUsagesMap.get(key);
                    ProductUsage matchedUsage = null;

                    if (candidates != null && !candidates.isEmpty()) {
                        Iterator<ProductUsage> iterator = candidates.iterator();
                        while (iterator.hasNext()) {
                            ProductUsage candidate = iterator.next();
                            if (textsAreSame(candidate.getDosage(), dosage) &&
                                    textsAreSame(candidate.getApplicationTiming(), applicationTiming)) {
                                matchedUsage = candidate;
                                iterator.remove();
                                break;
                            }
                        }
                    }

                    if (matchedUsage == null) {
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

                        if (!matchedUsage.isActive()) {
                            matchedUsage.setActive(true);
                            needsUpdate = true;
                        }

                        boolean isMinor = "TAK".equalsIgnoreCase(minorUseStr);
                        if (matchedUsage.isMinorUse() != isMinor) {
                            matchedUsage.setMinorUse(isMinor);
                            needsUpdate = true;
                        }

                        if (!Objects.equals(matchedUsage.getDosage(), dosage)) {
                            matchedUsage.setDosage(dosage);
                            needsUpdate = true;
                        }
                        if (!Objects.equals(matchedUsage.getApplicationTiming(), applicationTiming)) {
                            matchedUsage.setApplicationTiming(applicationTiming);
                            needsUpdate = true;
                        }

                        if (needsUpdate) {
                            this.usagesToSaveOrUpdate.add(matchedUsage);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("    [ProductUsageProcessor] Błąd wiersza: " + row.getRowNum() + ". " + e.getMessage());
        }
    }

    private boolean textsAreSame(String dbText, String fetchedText) {
        if (dbText == null) dbText = "";
        if (fetchedText == null) fetchedText = "";
        String normDb = dbText.trim().replaceAll("\\s+", " ").toLowerCase();
        String normFetch = fetchedText.trim().replaceAll("\\s+", " ").toLowerCase();
        return normDb.equals(normFetch);
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }
}
