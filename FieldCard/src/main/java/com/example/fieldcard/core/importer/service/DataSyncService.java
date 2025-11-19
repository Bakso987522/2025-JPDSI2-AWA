package com.example.fieldcard.core.importer.service;

import com.example.fieldcard.dto.external.AttributesDto;
import com.example.fieldcard.dto.external.ResourceDto;
import com.example.fieldcard.dto.external.ResourceListDto;
import com.example.fieldcard.core.importer.processor.FileProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataSyncService {

    private final String API_URL = "https://api.dane.gov.pl/1.4/datasets/550,wyszukiwarka-srodkow-ochrony-roslin-1/resources?sort=-data_date&per_page=50";
    private final WebClient webClient;
    private final List<FileProcessor> fileProcessors;

    @Autowired
    public DataSyncService(WebClient webClient, List<FileProcessor> fileProcessors) {
        this.webClient = webClient;
        this.fileProcessors = fileProcessors;
        System.out.println("DataSyncService załadowany. Znaleziono procesorów: " + fileProcessors.size());
    }

    @Scheduled(initialDelay = 2000, fixedRate = 3600000)
    public void syncData() {
        System.out.println("Uruchamiam synchronizację danych...");
        System.out.println("Pobieranie listy plików z: " + API_URL);

        ResourceListDto responseDto = this.webClient.get()
                .uri(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ResourceListDto.class)
                .block();

        if (responseDto == null || responseDto.getData() == null) {
            System.out.println("Otrzymano pustą odpowiedź.");
            return;
        }

        Map<String, ResourceDto> latestFiles = new HashMap<>();
        System.out.println("Otrzymano " + responseDto.getData().size() + " plików. Filtrowanie do najnowszych...");

        for (ResourceDto resource : responseDto.getData()) {
            AttributesDto attributes = resource.getAttributes();
            if (attributes == null) continue;

            String baseTitle = getBaseTitle(attributes.getTitle());
            latestFiles.putIfAbsent(baseTitle, resource);
        }

        System.out.println("--- ZNALEZIONE NAJNOWSZE PLIKI (" + latestFiles.size() + " szt.) ---");
        System.out.println("\n--- ROZPOCZYNAM POBIERANIE I PARSOWANIE PLIKÓW (W KOLEJNOŚCI @Order) ---");

        for (FileProcessor processor : this.fileProcessors) {

            String supportedTitle = null;
            ResourceDto resourceToProcess = null;

            for (Map.Entry<String, ResourceDto> entry : latestFiles.entrySet()) {
                String baseTitle = entry.getKey();
                if (processor.supports(baseTitle)) {
                    supportedTitle = baseTitle;
                    resourceToProcess = entry.getValue();
                    break;
                }
            }

            if (resourceToProcess != null) {
                AttributesDto attributes = resourceToProcess.getAttributes();
                String fileUrl = attributes.getLink();
                String title = attributes.getTitle();
                String format = attributes.getFormat();

                System.out.println("Przetwarzanie: " + title + " (Format: " + format + ")");

                try {
                    byte[] fileBytes = this.webClient.get()
                            .uri(fileUrl)
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .block();

                    if (fileBytes != null && fileBytes.length > 0) {
                        System.out.println("  Pobrano " + fileBytes.length + " bajtów.");
                        processor.process(fileBytes);
                        latestFiles.remove(supportedTitle);
                    } else {
                        System.out.println("  Nie udało się pobrać zawartości (pusty plik).");
                    }

                } catch (Exception e) {
                    System.out.println("  Błąd podczas pobierania pliku " + title + ": " + e.getMessage());
                }
                System.out.println("--------------------");
            }
        }

        if (!latestFiles.isEmpty()) {
            System.out.println("--- POMINIĘTE PLIKI (NIE ZNALEZIONO PROCESORA) ---");
            for (ResourceDto resource : latestFiles.values()) {
                System.out.println("  Nie znaleziono procesora dla: " + getBaseTitle(resource.getAttributes().getTitle()));
            }
            System.out.println("--------------------");
        }

        System.out.println("--- POBIERANIE ZAKOŃCZONE ---");
    }

    private String getBaseTitle(String fullTitle) {
        if (fullTitle == null) return "bez_tytulu";
        String[] parts = fullTitle.split(" - ");
        return parts.length > 0 ? parts[0].trim() : fullTitle.trim();
    }
}