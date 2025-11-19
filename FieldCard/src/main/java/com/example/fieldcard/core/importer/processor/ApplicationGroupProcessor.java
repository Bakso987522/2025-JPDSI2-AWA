package com.example.fieldcard.core.importer.processor;

import com.example.fieldcard.data.entity.ApplicationGroup;
import com.example.fieldcard.data.repository.ApplicationGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Order(3)
@Component
public class ApplicationGroupProcessor extends AbstractCsvProcessor {

    private final ApplicationGroupRepository repository;

    private Map<String, ApplicationGroup> existingGroupsMap;
    private List<ApplicationGroup> groupsToSaveOrUpdate;

    @Autowired
    public ApplicationGroupProcessor(ApplicationGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(String baseTitle) {
        return "słownik grup stosowania".equals(baseTitle);
    }

    @Override
    @Transactional
    public void process(String fileContent) {
        System.out.println("    [ApplicationGroupProcessor] Rozpoczynam SYNCHRONIZACJĘ 'grupy zastosowan'...");

        this.groupsToSaveOrUpdate = new ArrayList<>();

        List<ApplicationGroup> existingGroups = repository.findAll();


        this.existingGroupsMap = existingGroups.stream()
                .collect(Collectors.toMap(ApplicationGroup::getName, Function.identity()));


        super.process(fileContent);


        int deactivatedCount = 0;
        if (!existingGroupsMap.isEmpty()) {
            for (ApplicationGroup groupToDeactivate : existingGroupsMap.values()) {
                if (groupToDeactivate.isActive()) {
                    groupToDeactivate.setActive(false);
                    this.groupsToSaveOrUpdate.add(groupToDeactivate);
                    deactivatedCount++;
                }
            }
        }


        if (!this.groupsToSaveOrUpdate.isEmpty()) {
            repository.saveAll(this.groupsToSaveOrUpdate);
        }

        System.out.println("    [ApplicationGroupProcessor] Synchronizacja zakończona.");
        System.out.println("    [ApplicationGroupProcessor] Przetworzono (zapis/aktualizacja): " + groupsToSaveOrUpdate.size() + " rekordów.");
        System.out.println("    [ApplicationGroupProcessor] Oznaczono jako nieaktywne: " + deactivatedCount + " rekordów.");
    }

    @Override
    protected void processRow(String[] record) {

        if (record.length < 2) return;

        String name = record[1];

        if (name == null || name.trim().isEmpty() || "nazwa".equalsIgnoreCase(name)) {
            return;
        }

        name = name.trim();


        ApplicationGroup existingGroup = this.existingGroupsMap.remove(name);


        if (existingGroup == null) {

            ApplicationGroup newGroup = new ApplicationGroup(name);
            newGroup.setActive(true);
            this.groupsToSaveOrUpdate.add(newGroup);

        } else {

            if (!existingGroup.isActive()) {
                existingGroup.setActive(true);
                this.groupsToSaveOrUpdate.add(existingGroup);
            }

        }
    }
}