package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.FormulationType;
import com.example.fieldcard.repository.FormulationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FormulationTypeProcessor extends AbstractCsvProcessor {

    private final FormulationTypeRepository formulationTypeRepository;

    private Map<String, FormulationType> existingTypesMap;
    private List<FormulationType> typesToSaveOrUpdate;

    @Autowired
    public FormulationTypeProcessor(FormulationTypeRepository formulationTypeRepository) {
        this.formulationTypeRepository = formulationTypeRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik rodzajów preparatu".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(String fileContent) {
        System.out.println("    [FormulationTypeProcessor] Rozpoczynam SYNCHRONIZACJĘ 'słownik rodzajów preparatu' (Soft Delete)...");

        List<FormulationType> existingTypesList = formulationTypeRepository.findAll();

        this.existingTypesMap = existingTypesList.stream()
                .collect(Collectors.toMap(FormulationType::getName, Function.identity()));

        System.out.println("    [FormulationTypeProcessor] Znaleziono " + this.existingTypesMap.size() + " wszystkich typów w bazie.");

        this.typesToSaveOrUpdate = new ArrayList<>();

        super.process(fileContent);

        List<FormulationType> typesToDeactivate = new ArrayList<>(this.existingTypesMap.values());

        int deactivatedCount = 0;
        for (FormulationType type : typesToDeactivate) {
            if (type.isActive()) {
                type.setActive(false);
                this.typesToSaveOrUpdate.add(type);
                deactivatedCount++;
            }
        }

        if (!typesToSaveOrUpdate.isEmpty()) {
            formulationTypeRepository.saveAll(typesToSaveOrUpdate);
        }

        System.out.println("    [FormulationTypeProcessor] Synchronizacja zakończona.");
        System.out.println("    [FormulationTypeProcessor] Przetworzono (zapis/aktualizacja): " + typesToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [FormulationTypeProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    @Override
    protected void processRow(String[] nextRecord) {
        try {
            if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

                String typeName = nextRecord[0].trim();

                FormulationType existingType = this.existingTypesMap.remove(typeName);

                if (existingType == null) {
                    this.typesToSaveOrUpdate.add(new FormulationType(typeName));

                } else {
                    if (!existingType.isActive()) {
                        existingType.setActive(true);
                        this.typesToSaveOrUpdate.add(existingType);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("    [FormulationTypeProcessor] Błąd wierszu: " + String.join(";", nextRecord)
                    + ". Błąd: " + e.getMessage());
        }
    }
}