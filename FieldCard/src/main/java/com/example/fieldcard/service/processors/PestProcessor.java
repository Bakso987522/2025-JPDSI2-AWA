package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.Pest;
import com.example.fieldcard.repository.PestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PestProcessor extends AbstractCsvProcessor{
    private final PestRepository pestRepository;
    private List<Pest> pestsToSave;

    @Autowired
    public PestProcessor(PestRepository pestRepository) {
        this.pestRepository = pestRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik agrofagów".equals(baseTitle);
    }

    @Override
    public void process(String fileContent) {
        System.out.println("    [PestProcessor] Rozpoczynam parsowanie 'słownik agrofagów'...");

        this.pestsToSave = new ArrayList<>();
        pestRepository.deleteAllInBatch();

        super.process(fileContent);

        pestRepository.saveAll(this.pestsToSave);
        System.out.println("    [PestProcessor] Zapisano " + this.pestsToSave.size() + "agrofagów do bazy.");
    }

    @Override
    protected void processRow(String[] nextRecord) {
        if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

            String pestName = nextRecord[0].trim();

            this.pestsToSave.add(new Pest(pestName));
        }
    }
}
