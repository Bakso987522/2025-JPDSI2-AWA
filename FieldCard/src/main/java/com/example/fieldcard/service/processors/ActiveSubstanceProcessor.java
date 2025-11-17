package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.ActiveSubstance;
import com.example.fieldcard.repository.ActiveSubstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Order(2)
@Component
public class ActiveSubstanceProcessor extends AbstractCsvProcessor {

    private final ActiveSubstanceRepository activeSubstanceRepository;

    private Map<String, ActiveSubstance> existingSubstancesMap;
    private List<ActiveSubstance> substancesToSaveOrUpdate;

    @Autowired
    public ActiveSubstanceProcessor(ActiveSubstanceRepository activeSubstanceRepository) {
        this.activeSubstanceRepository = activeSubstanceRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik substancji aktywnych".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(String fileContent) {
        System.out.println("    [ActiveSubstanceProcessor] Rozpoczynam SYNCHRONIZACJĘ 'słownik substancji aktywnych' (Soft Delete)...");

        List<ActiveSubstance> existingSubstancesList = activeSubstanceRepository.findAll();

        this.existingSubstancesMap = existingSubstancesList.stream()
                .collect(Collectors.toMap(ActiveSubstance::getName, Function.identity()));

        System.out.println("    [ActiveSubstanceProcessor] Znaleziono " + this.existingSubstancesMap.size() + " wszystkich substancji w bazie.");

        this.substancesToSaveOrUpdate = new ArrayList<>();

        super.process(fileContent);

        List<ActiveSubstance> substancesToDeactivate = new ArrayList<>(this.existingSubstancesMap.values());

        int deactivatedCount = 0;
        for (ActiveSubstance substance : substancesToDeactivate) {
            if (substance.isActive()) {
                substance.setActive(false);
                this.substancesToSaveOrUpdate.add(substance);
                deactivatedCount++;
            }
        }

        if (!substancesToSaveOrUpdate.isEmpty()) {
            activeSubstanceRepository.saveAll(substancesToSaveOrUpdate);
        }

        System.out.println("    [ActiveSubstanceProcessor] Synchronizacja zakończona.");
        System.out.println("    [ActiveSubstanceProcessor] Przetworzono (zapis/aktualizacja): " + substancesToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [ActiveSubstanceProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    @Override
    protected void processRow(String[] nextRecord) {
        try {
            if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

                String substanceName = nextRecord[0].trim();

                ActiveSubstance existingSubstance = this.existingSubstancesMap.remove(substanceName);

                if (existingSubstance == null) {
                    this.substancesToSaveOrUpdate.add(new ActiveSubstance(substanceName));

                } else {
                    if (!existingSubstance.isActive()) {
                        existingSubstance.setActive(true);
                        this.substancesToSaveOrUpdate.add(existingSubstance);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("    [ActiveSubstanceProcessor] Błąd wierszu: " + String.join(";", nextRecord)
                    + ". Błąd: " + e.getMessage());
        }
    }
}