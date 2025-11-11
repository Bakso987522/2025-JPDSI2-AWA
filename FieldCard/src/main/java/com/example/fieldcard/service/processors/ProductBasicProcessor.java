package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.PlantProtectionProduct;
import com.example.fieldcard.repository.PlantProtectionProductRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductBasicProcessor implements FileProcessor {

    private final PlantProtectionProductRepository productRepository;

    private Map<String, PlantProtectionProduct> existingProductsMap;
    private List<PlantProtectionProduct> productsToSaveOrUpdate;

    @Autowired
    public ProductBasicProcessor(PlantProtectionProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "rejestr podstawowy".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(byte[] fileContent) {
        System.out.println("    [ProductBasicProcessor] Rozpoczynam SYNCHRONIZACJĘ 'Rejestr_podstawowe' (XLSX)...");

        List<PlantProtectionProduct> existingProductsList = productRepository.findAll();
        this.existingProductsMap = existingProductsList.stream()
                .collect(Collectors.toMap(PlantProtectionProduct::getSorId, Function.identity()));

        System.out.println("    [ProductBasicProcessor] Znaleziono " + this.existingProductsMap.size() + " wszystkich produktów w bazie.");

        this.productsToSaveOrUpdate = new ArrayList<>();

        // Użyj Apache POI do czytania byte[]
        try (InputStream is = new ByteArrayInputStream(fileContent);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // Pierwszy arkusz

            // Iteruj po wierszach, pomijając nagłówek (i=0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    processRow(row); // Przekaż obiekt Row do processRow
                }
            }

        } catch (Exception e) {
            System.out.println("    [ProductBasicProcessor] Błąd podczas parsowania XLSX: " + e.getMessage());
        }

        // Logika Soft-Delete
        List<PlantProtectionProduct> productsToDeactivate = new ArrayList<>(this.existingProductsMap.values());
        int deactivatedCount = 0;
        for (PlantProtectionProduct product : productsToDeactivate) {
            if (product.isActive()) {
                product.setActive(false);
                this.productsToSaveOrUpdate.add(product);
                deactivatedCount++;
            }
        }

        if (!productsToSaveOrUpdate.isEmpty()) {
            productRepository.saveAll(productsToSaveOrUpdate);
        }

        System.out.println("    [ProductBasicProcessor] Synchronizacja zakończona.");
        System.out.println("    [ProductBasicProcessor] Przetworzono (zapis/aktualizacja): " + productsToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [ProductBasicProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    /**
     * Przetwarza pojedynczy wiersz z pliku Excel (obiekt Row).
     */
    protected void processRow(Row row) {
        try {
            // Indeksy kolumn
            String sorId = getStringCellValue(row.getCell(0)); // A
            String name = getStringCellValue(row.getCell(2)); // C
            String manufacturer = getStringCellValue(row.getCell(3)); // D
            String permitNumber = getStringCellValue(row.getCell(4)); // E
            String productType = getStringCellValue(row.getCell(5)); // F
            String activeSubstancesString = getStringCellValue(row.getCell(6)); // G
            LocalDate permitDate = getDateCellValue(row.getCell(7)); // H
            LocalDate salesDeadline = getDateCellValue(row.getCell(8)); // I
            LocalDate useDeadline = getDateCellValue(row.getCell(9)); // J
            String labelUrl = getStringCellValue(row.getCell(10)); // K

            if (sorId == null || sorId.trim().isEmpty()) {
                return;
            }

            PlantProtectionProduct existingProduct = this.existingProductsMap.remove(sorId);

            if (existingProduct == null) {
                // NOWY PRODUKT
                PlantProtectionProduct newProduct = new PlantProtectionProduct();
                newProduct.setSorId(sorId);
                newProduct.setName(name);
                newProduct.setManufacturer(manufacturer);
                newProduct.setPermitNumber(permitNumber);
                newProduct.setProductType(productType);
                newProduct.setActiveSubstancesString(activeSubstancesString);
                newProduct.setPermitDate(permitDate);
                newProduct.setSalesDeadline(salesDeadline);
                newProduct.setUseDeadline(useDeadline);
                newProduct.setLabelUrl(labelUrl);
                newProduct.setActive(true);

                this.productsToSaveOrUpdate.add(newProduct);

            } else {
                // ISTNIEJĄCY PRODUKT (aktualizacja + reaktywacja)
                boolean needsUpdate = false;
                if (!existingProduct.isActive()) {
                    existingProduct.setActive(true);
                    needsUpdate = true;
                }

                if (!Objects.equals(existingProduct.getName(), name)) {
                    existingProduct.setName(name);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getManufacturer(), manufacturer)) {
                    existingProduct.setManufacturer(manufacturer);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getPermitNumber(), permitNumber)) {
                    existingProduct.setPermitNumber(permitNumber);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getProductType(), productType)) {
                    existingProduct.setProductType(productType);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getActiveSubstancesString(), activeSubstancesString)) {
                    existingProduct.setActiveSubstancesString(activeSubstancesString);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getPermitDate(), permitDate)) {
                    existingProduct.setPermitDate(permitDate);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getSalesDeadline(), salesDeadline)) {
                    existingProduct.setSalesDeadline(salesDeadline);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getUseDeadline(), useDeadline)) {
                    existingProduct.setUseDeadline(useDeadline);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getLabelUrl(), labelUrl)) {
                    existingProduct.setLabelUrl(labelUrl);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    this.productsToSaveOrUpdate.add(existingProduct);
                }
            }
        } catch (Exception e) {
            System.out.println("    [ProductBasicProcessor] Błąd wierszu Excela: " + row.getRowNum()
                    + ". Błąd: " + e.getMessage());
        }
    }

    /**
     * Pomocnicza metoda do bezpiecznego pobierania wartości String z komórki.
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            // Traktuj numeryczne jako tekst
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }

    /**
     * Pomocnicza metoda do bezpiecznego pobierania wartości LocalDate z komórki.
     */
    private LocalDate getDateCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                // Data w Excelu jest często liczbą
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
            if (cell.getCellType() == CellType.STRING) {
                // Spróbuj parsować datę ze stringa
                return LocalDate.parse(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {
            // Ignoruj błędy parsowania daty, po prostu zwróć null
        }
        return null;
    }
}