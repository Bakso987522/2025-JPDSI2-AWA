package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.Crop;
import com.example.fieldcard.repository.CropRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CropProcessor extends AbstractCsvProcessor {

    private final CropRepository cropRepository;

    private Map<String, Crop> existingCropsMap;
    private List<Crop> cropsToSaveOrUpdate;

    @Autowired
    public CropProcessor(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik uprawy".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(String fileContent) {
        System.out.println("    [CropProcessor] Rozpoczynam SYNCHRONIZACJĘ 'słownik uprawy' (Soft Delete)...");

        List<Crop> existingCropsList = cropRepository.findAll();

        this.existingCropsMap = existingCropsList.stream()
                .collect(Collectors.toMap(Crop::getName, Function.identity()));

        System.out.println("    [CropProcessor] Znaleziono " + this.existingCropsMap.size() + " wszystkich upraw w bazie.");

        this.cropsToSaveOrUpdate = new ArrayList<>();

        super.process(fileContent);

        List<Crop> cropsToDeactivate = new ArrayList<>(this.existingCropsMap.values());

        int deactivatedCount = 0;
        for (Crop crop : cropsToDeactivate) {
            if (crop.isActive()) {
                crop.setActive(false);
                this.cropsToSaveOrUpdate.add(crop);
                deactivatedCount++;
            }
        }

        if (!cropsToSaveOrUpdate.isEmpty()) {
            cropRepository.saveAll(cropsToSaveOrUpdate);
        }

        System.out.println("    [CropProcessor] Synchronizacja zakończona.");
        System.out.println("    [CropProcessor] Przetworzono (zapis/aktualizacja): " + cropsToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [CropProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    @Override
    protected void processRow(String[] nextRecord) {
        try {
            if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

                String cropName = nextRecord[0].trim();

                Crop existingCrop = this.existingCropsMap.remove(cropName);

                if (existingCrop == null) {
                    this.cropsToSaveOrUpdate.add(new Crop(cropName));

                } else {
                    if (!existingCrop.isActive()) {
                        existingCrop.setActive(true);
                        this.cropsToSaveOrUpdate.add(existingCrop);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("    [CropProcessor] Błąd wierszu: " + String.join(";", nextRecord)
                    + ". Błąd: " + e.getMessage());
        }
    }
}