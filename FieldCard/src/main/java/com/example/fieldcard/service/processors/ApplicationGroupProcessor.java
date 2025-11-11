package com.example.fieldcard.service.processors;

import com.example.fieldcard.entity.ApplicationGroup;
import com.example.fieldcard.repository.ApplicationGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ApplicationGroupProcessor extends AbstractCsvProcessor {
    private final ApplicationGroupRepository applicationGroupRepository;

    private Map<Long, ApplicationGroup> existingGroupsMap;
    private List<ApplicationGroup> groupsToSaveOrUpdate;

    @Autowired
    public ApplicationGroupProcessor(ApplicationGroupRepository applicationGroupRepository) {
        this.applicationGroupRepository = applicationGroupRepository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik grup stosowania".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(String fileContent) {
        System.out.println("    [ApplicationGroupProcessor] Rozpoczynam SYNCHRONIZACJĘ 'słownik grup stosowania' (Soft Delete)...");

        List<ApplicationGroup> existingGroupsList = applicationGroupRepository.findAll();

        this.existingGroupsMap = existingGroupsList.stream()
                .collect(Collectors.toMap(ApplicationGroup::getGroupId, Function.identity()));

        System.out.println("    [ApplicationGroupProcessor] Znaleziono " + this.existingGroupsMap.size() + " wszystkich grup w bazie.");

        this.groupsToSaveOrUpdate = new ArrayList<>();

        super.process(fileContent);

        List<ApplicationGroup> groupsToDeactivate = new ArrayList<>(this.existingGroupsMap.values());

        int deactivatedCount = 0;
        for (ApplicationGroup group : groupsToDeactivate) {
            if (group.isActive()) {
                group.setActive(false);
                this.groupsToSaveOrUpdate.add(group);
                deactivatedCount++;
            }
        }

        if (!this.groupsToSaveOrUpdate.isEmpty()) {
            applicationGroupRepository.saveAll(this.groupsToSaveOrUpdate);
        }

        System.out.println("    [ApplicationGroupProcessor] Synchronizacja zakończona.");
        System.out.println("    [ApplicationGroupProcessor] Przetworzono (zapis/aktualizacja): " + groupsToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [ApplicationGroupProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    @Override
    protected void processRow(String[] nextRecord) {
        try {
            if (nextRecord.length >= 2 &&
                    nextRecord[0] != null && !nextRecord[0].trim().isEmpty() &&
                    nextRecord[1] != null && !nextRecord[1].trim().isEmpty()) {

                Long groupId = Long.parseLong(nextRecord[0].trim());
                String name = nextRecord[1].trim();

                ApplicationGroup existingGroup = this.existingGroupsMap.remove(groupId);

                if (existingGroup == null) {
                    this.groupsToSaveOrUpdate.add(new ApplicationGroup(groupId, name));
                } else {
                    boolean needsUpdate = false;

                    if (!existingGroup.isActive()) {
                        existingGroup.setActive(true);
                        needsUpdate = true;
                    }

                    if (!existingGroup.getName().equals(name)) {
                        existingGroup.setName(name);
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        this.groupsToSaveOrUpdate.add(existingGroup);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("    [ApplicationGroupProcessor] Błąd parsowania numeru grupy: '" + nextRecord[0] + "' - Pomijanie wiersza.");
        } catch (Exception e) {
            System.out.println("    [ApplicationGroupProcessor] Błąd wierszu: " + String.join(";", nextRecord)
                    + ". Błąd: " + e.getMessage());
        }
    }
}