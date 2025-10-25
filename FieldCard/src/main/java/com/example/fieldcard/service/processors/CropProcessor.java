package com.example.fieldcard.service.processors;

import com.example.fieldcard.repository.CropRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.fieldcard.entity.Crop;
import java.util.ArrayList;
import java.util.List;

@Component
public class CropProcessor extends AbstractCsvProcessor{
    private final CropRepository cropRepository;
    private List<Crop> cropsToSave;

    @Autowired
    public CropProcessor(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik uprawy".equals(baseTitle);
    }


    @Override
    public void process(String fileContent) {
        System.out.println("    [CropProcessor] Rozpoczynam parsowanie 'słownik uprawy'...");

        this.cropsToSave = new ArrayList<>();
        cropRepository.deleteAllInBatch();

        super.process(fileContent);

        cropRepository.saveAll(this.cropsToSave);
        System.out.println("    [CropProcessor] Zapisano " + this.cropsToSave.size() + " upraw do bazy.");
    }


    @Override
    protected void processRow(String[] nextRecord) {
        if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

            String cropName = nextRecord[0].trim();

            this.cropsToSave.add(new Crop(cropName));
        }
    }
}
