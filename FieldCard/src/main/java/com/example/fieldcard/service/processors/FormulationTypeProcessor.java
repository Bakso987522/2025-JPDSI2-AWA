package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.FormulationType;
import com.example.fieldcard.repository.FormulationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class FormulationTypeProcessor extends AbstractCsvProcessor{
    final FormulationTypeRepository formulationTypeRepository;
    private List<FormulationType> formulationTypesToSave;
    @Autowired
    public FormulationTypeProcessor(FormulationTypeRepository formulationTypeRepository) {
        this.formulationTypeRepository = formulationTypeRepository;
    }
    @Override
    public boolean supports(String baseTitle) {
        return "słownik rodzajów preparatu".equals(baseTitle);
    }
    @Override
    public void process(String fileContent) {
        System.out.println("    [FormulationTypeProcessor] Rozpoczynam parsowanie 'słownik rodzajów preparatu'...");

        this.formulationTypesToSave = new ArrayList<>();
        formulationTypeRepository.deleteAllInBatch();

        super.process(fileContent);

        formulationTypeRepository.saveAll(this.formulationTypesToSave);
        System.out.println("    [FormulationTypeProcessor] Zapisano " + this.formulationTypesToSave.size() + " rodzajów preparatu do bazy.");
    }
    @Override
    protected void processRow(String[] nextRecord) {
        if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

            String formulationType = nextRecord[0].trim();

            this.formulationTypesToSave.add(new FormulationType(formulationType));
        }
    }
}
