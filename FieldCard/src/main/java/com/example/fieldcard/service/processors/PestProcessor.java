package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.Pest;
import com.example.fieldcard.repository.PestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PestProcessor extends AbstractCsvProcessor {
    private final PestRepository pestRepository;

    private Map<String, Pest> existingPestsMap;
    private List<Pest> pestsToSaveOrUpdate;

    @Autowired
    public PestProcessor(PestRepository pestRepository) {
        this.pestRepository = pestRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik agrofagów".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(String fileContent) {
        System.out.println("    [PestProcessor] Rozpoczynam SYNCHRONIZACJĘ 'słownik agrofagów' (Soft Delete)...");

        List<Pest> existingPestsList = pestRepository.findAll();

        this.existingPestsMap = existingPestsList.stream()
                .collect(Collectors.toMap(Pest::getName, Function.identity()));

        System.out.println("    [PestProcessor] Znaleziono " + this.existingPestsMap.size() + " wszystkich agrofagów w bazie.");

        this.pestsToSaveOrUpdate = new ArrayList<>();

        super.process(fileContent);

        List<Pest> pestsToDeactivate = new ArrayList<>(this.existingPestsMap.values());

        int deactivatedCount = 0;
        for (Pest pest : pestsToDeactivate) {
            if (pest.isActive()) {
                pest.setActive(false);
                this.pestsToSaveOrUpdate.add(pest);
                deactivatedCount++;
            }
        }

        if (!pestsToSaveOrUpdate.isEmpty()) {
            pestRepository.saveAll(pestsToSaveOrUpdate);
        }

        System.out.println("    [PestProcessor] Synchronizacja zakończona.");
        System.out.println("    [PestProcessor] Przetworzono (zapis/aktualizacja): " + pestsToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [PestProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    @Override
    protected void processRow(String[] nextRecord) {
        try {
            if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {

                String pestName = nextRecord[0].trim();

                Pest existingPest = this.existingPestsMap.remove(pestName);

                if (existingPest == null) {
                    this.pestsToSaveOrUpdate.add(new Pest(pestName));

                } else {
                    if (!existingPest.isActive()) {
                        existingPest.setActive(true);
                        this.pestsToSaveOrUpdate.add(existingPest);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("    [PestProcessor] Błąd wierszu: " + String.join(";", nextRecord)
                    + ". Błąd: " + e.getMessage());
        }
    }
}