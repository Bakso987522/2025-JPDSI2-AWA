package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.ActiveSubstance;
import com.example.fieldcard.repository.ActiveSubstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActiveSubstanceProcessor extends AbstractCsvProcessor {
    final ActiveSubstanceRepository activeSubstanceRepository;
    private List<ActiveSubstance> activeSubstancesToSave;

    @Autowired
    public ActiveSubstanceProcessor(ActiveSubstanceRepository activeSubstanceRepository) {
        this.activeSubstanceRepository = activeSubstanceRepository;
    }
    @Override
    public boolean supports(String baseTitle) {
        return "słownik substancji aktywnych".equals(baseTitle);
    }
    @Override
    public void process(String fileContent){
        System.out.println("    [ActiveSubstanceProcessor] Rozpoczynam parsowanie 'słownik substancji aktywnych'...");

        this.activeSubstancesToSave = new ArrayList<>();
        activeSubstanceRepository.deleteAllInBatch();

        super.process(fileContent);

        activeSubstanceRepository.saveAll(this.activeSubstancesToSave);
        System.out.println("    [ActiveSubstanceProcessor] Zapisano " + this.activeSubstancesToSave.size() + " substancji aktywnych do bazy.");
    }
    @Override
    public void processRow(String[] nextRecord){
        if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

            String activeSubstance = nextRecord[0].trim();

            this.activeSubstancesToSave.add(new ActiveSubstance(activeSubstance));
        }
    }
}
