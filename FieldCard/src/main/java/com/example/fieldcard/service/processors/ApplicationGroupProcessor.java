package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.ApplicationGroup;
import com.example.fieldcard.repository.ApplicationGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class ApplicationGroupProcessor extends  AbstractCsvProcessor {
    private final ApplicationGroupRepository applicationGroupRepository;
    private List<ApplicationGroup> applicationGroupsToSave;

    @Autowired
    public ApplicationGroupProcessor(ApplicationGroupRepository applicationGroupRepository) {
        this.applicationGroupRepository = applicationGroupRepository;
    }
    @Override
    public boolean supports(String baseTitle) {
        return "słownik grup stosowania".equals(baseTitle);
    }

    @Override
    public void process(String fileContent) {
        System.out.println("    [ApplicationGroupProcessor] Rozpoczynam parsowanie 'słownik grup stosowania'...");

        this.applicationGroupsToSave = new ArrayList<>();
        applicationGroupRepository.deleteAllInBatch();

        super.process(fileContent);

        applicationGroupRepository.saveAll(this.applicationGroupsToSave);
        System.out.println("    [ApplicationGroupProcessor] Zapisano " + this.applicationGroupsToSave.size() + " grup stosowania do bazy.");
    }

    @Override
    protected void processRow(String[] nextRecord) {
        if (nextRecord.length >= 2 &&
                nextRecord[0] != null && !nextRecord[0].trim().isEmpty() &&
                nextRecord[1] != null && !nextRecord[1].trim().isEmpty()) {

            try {
                Long groupId = Long.parseLong(nextRecord[0].trim());
                String name = nextRecord[1].trim();

                this.applicationGroupsToSave.add(new ApplicationGroup(groupId, name));
            } catch (NumberFormatException e) {
                System.out.println("    Błąd parsowania numeru grupy: '" + nextRecord[0] + "' - Pomijanie wiersza.");
            }
        }
    }
}
