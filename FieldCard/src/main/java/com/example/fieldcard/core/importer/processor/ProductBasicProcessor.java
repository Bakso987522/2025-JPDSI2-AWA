package com.example.fieldcard.core.importer.processor;

import com.example.fieldcard.data.entity.ActiveSubstance;
import com.example.fieldcard.data.entity.ProductActiveSubstance;
import com.example.fieldcard.data.entity.ProductType;
import com.example.fieldcard.data.entity.PlantProtectionProduct;
import com.example.fieldcard.data.repository.ActiveSubstanceRepository;
import com.example.fieldcard.data.repository.ProductTypeRepository;
import com.example.fieldcard.data.repository.PlantProtectionProductRepository;
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
    private final ActiveSubstanceRepository activeSubstanceRepository;

    private Map<String, PlantProtectionProduct> existingProductsMap;
    private List<PlantProtectionProduct> productsToSaveOrUpdate;
    private Map<String, ActiveSubstance> activeSubstanceCache;

    @Autowired
    public ProductBasicProcessor(PlantProtectionProductRepository productRepository,
                                 ProductTypeRepository productTypeRepository,
                                 ActiveSubstanceRepository activeSubstanceRepository) {
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.activeSubstanceRepository = activeSubstanceRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "rejestr podstawowy".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(byte[] fileContent) {
        System.out.println("    [ProductBasicProcessor] Rozpoczynam SYNCHRONIZACJĘ 'Rejestr_podstawowe' (XLSX)...");

        loadActiveSubstanceCache();

        List<PlantProtectionProduct> existingProductsList = productRepository.findAllWithProductTypesAndSubstances();
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

        this.activeSubstanceCache.clear();

        System.out.println("    [ProductBasicProcessor] Synchronizacja zakończona.");
        System.out.println("    [ProductBasicProcessor] Przetworzono (zapis/aktualizacja): " + productsToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [ProductBasicProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    private void loadActiveSubstanceCache() {
        this.activeSubstanceCache = activeSubstanceRepository.findAllByIsActive(true).stream()
                .collect(Collectors.toMap(ActiveSubstance::getName, Function.identity()));
        System.out.println("    [ProductBasicProcessor] Załadowano " + this.activeSubstanceCache.size() + " substancji aktywnych do cache.");
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

    private Set<ProductActiveSubstance> parseActiveSubstances(String rawString, PlantProtectionProduct product) {
        if (rawString == null || rawString.trim().isEmpty()) {
            return new HashSet<>();
        }

        Set<ProductActiveSubstance> newLinks = new HashSet<>();
        String[] parts = rawString.split(",");

        for (String part : parts) {
            String[] substanceParts = part.split("-", 2);

            if (substanceParts.length == 2) {
                String name = substanceParts[0].trim();
                String content = substanceParts[1].trim();

                ActiveSubstance substance = this.activeSubstanceCache.get(name);

                if (substance != null) {
                    ProductActiveSubstance newLink = new ProductActiveSubstance();
                    newLink.setProduct(product);
                    newLink.setActiveSubstance(substance);
                    newLink.setContent(content);
                    newLinks.add(newLink);
                }
            }
        }
        return newLinks;
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

                Set<ProductActiveSubstance> substanceLinks = parseActiveSubstances(activeSubstancesString, newProduct);
                newProduct.setActiveSubstances(substanceLinks);

                newProduct.setSorId(sorId);
                newProduct.setName(name);
                newProduct.setManufacturer(manufacturer);
                newProduct.setPermitNumber(permitNumber);
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

                Set<ProductActiveSubstance> newSubstanceLinks = parseActiveSubstances(activeSubstancesString, existingProduct);
                Set<ProductActiveSubstance> existingSubstanceLinks = existingProduct.getActiveSubstances();

                if (!existingSubstanceLinks.equals(newSubstanceLinks)) {
                    existingSubstanceLinks.clear();
                    existingSubstanceLinks.addAll(newSubstanceLinks);
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