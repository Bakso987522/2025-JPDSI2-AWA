package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.ProductType;
import com.example.fieldcard.entity.PlantProtectionProduct;
import com.example.fieldcard.repository.ProductTypeRepository;
import com.example.fieldcard.repository.PlantProtectionProductRepository;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Order(6)
@Component
public class ProductBasicProcessor implements FileProcessor {

    private final PlantProtectionProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;

    private Map<String, PlantProtectionProduct> existingProductsMap;
    private List<PlantProtectionProduct> productsToSaveOrUpdate;

    @Autowired
    public ProductBasicProcessor(PlantProtectionProductRepository productRepository,
                                 ProductTypeRepository productTypeRepository) {
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "rejestr podstawowy".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(byte[] fileContent) {
        System.out.println("    [ProductBasicProcessor] Rozpoczynam SYNCHRONIZACJĘ 'Rejestr_podstawowe' (XLSX)...");

        List<PlantProtectionProduct> existingProductsList = productRepository.findAllWithProductTypes();
        this.existingProductsMap = existingProductsList.stream()
                .collect(Collectors.toMap(PlantProtectionProduct::getSorId, Function.identity()));

        System.out.println("    [ProductBasicProcessor] Znaleziono " + this.existingProductsMap.size() + " wszystkich produktów w bazie.");

        this.productsToSaveOrUpdate = new ArrayList<>();

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
            System.out.println("    [ProductBasicProcessor] Błąd podczas parsowania XLSX: " + e.getMessage());
        }

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

    private Set<ProductType> parseProductTypes(String productTypeString) {
        if (productTypeString == null || productTypeString.trim().isEmpty()) {
            return new HashSet<>();
        }

        Set<String> typeNames = Arrays.stream(productTypeString.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        if (typeNames.isEmpty()) {
            return new HashSet<>();
        }

        List<ProductType> foundTypes = productTypeRepository.findAllByNameInAndIsActive(typeNames);

        return new HashSet<>(foundTypes);
    }

    protected void processRow(Row row) {
        try {
            String sorId = getStringCellValue(row.getCell(0));
            String name = getStringCellValue(row.getCell(2));
            String manufacturer = getStringCellValue(row.getCell(3));
            String permitNumber = getStringCellValue(row.getCell(4));
            String productTypeString = getStringCellValue(row.getCell(5));
            String activeSubstancesString = getStringCellValue(row.getCell(6));
            LocalDate permitDate = getDateCellValue(row.getCell(7));
            LocalDate salesDeadline = getDateCellValue(row.getCell(8));
            LocalDate useDeadline = getDateCellValue(row.getCell(9));
            String labelUrl = getStringCellValue(row.getCell(10));

            if (sorId == null || sorId.trim().isEmpty()) {
                return;
            }

            Set<ProductType> productTypes = parseProductTypes(productTypeString);

            PlantProtectionProduct existingProduct = this.existingProductsMap.remove(sorId);

            if (existingProduct == null) {
                PlantProtectionProduct newProduct = new PlantProtectionProduct();
                newProduct.setSorId(sorId);
                newProduct.setName(name);
                newProduct.setManufacturer(manufacturer);
                newProduct.setPermitNumber(permitNumber);
                newProduct.setActiveSubstancesString(activeSubstancesString);
                newProduct.setPermitDate(permitDate);
                newProduct.setSalesDeadline(salesDeadline);
                newProduct.setUseDeadline(useDeadline);
                newProduct.setLabelUrl(labelUrl);
                newProduct.setActive(true);
                newProduct.setProductTypes(productTypes);

                this.productsToSaveOrUpdate.add(newProduct);

            } else {
                boolean needsUpdate = false;

                if (!existingProduct.isActive()) {
                    existingProduct.setActive(true);
                    needsUpdate = true;
                }
                if (!Objects.equals(existingProduct.getName(), name)) { needsUpdate = true; existingProduct.setName(name); }
                if (!Objects.equals(existingProduct.getManufacturer(), manufacturer)) { needsUpdate = true; existingProduct.setManufacturer(manufacturer); }
                if (!Objects.equals(existingProduct.getPermitNumber(), permitNumber)) { needsUpdate = true; existingProduct.setPermitNumber(permitNumber); }
                if (!Objects.equals(existingProduct.getActiveSubstancesString(), activeSubstancesString)) { needsUpdate = true; existingProduct.setActiveSubstancesString(activeSubstancesString); }
                if (!Objects.equals(existingProduct.getPermitDate(), permitDate)) { needsUpdate = true; existingProduct.setPermitDate(permitDate); }
                if (!Objects.equals(existingProduct.getSalesDeadline(), salesDeadline)) { needsUpdate = true; existingProduct.setSalesDeadline(salesDeadline); }
                if (!Objects.equals(existingProduct.getUseDeadline(), useDeadline)) { needsUpdate = true; existingProduct.setUseDeadline(useDeadline); }
                if (!Objects.equals(existingProduct.getLabelUrl(), labelUrl)) { needsUpdate = true; existingProduct.setLabelUrl(labelUrl); }

                Set<ProductType> existingTypes = existingProduct.getProductTypes();
                if (!existingTypes.equals(productTypes)) {
                    existingTypes.clear();
                    existingTypes.addAll(productTypes);
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

    private String getStringCellValue(Cell cell) {
        if (cell == null) { return null; }
        if (cell.getCellType() == CellType.STRING) { return cell.getStringCellValue().trim(); }
        if (cell.getCellType() == CellType.NUMERIC) { return String.valueOf((long) cell.getNumericCellValue()); }
        return null;
    }

    private LocalDate getDateCellValue(Cell cell) {
        if (cell == null) { return null; }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            if (cell.getCellType() == CellType.STRING) {
                return LocalDate.parse(cell.getStringCellValue().trim());
            }
        } catch (Exception e) {}
        return null;
    }
}