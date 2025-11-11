package com.example.fieldcard.service;

import com.example.fieldcard.dto.AttributesDto;
import com.example.fieldcard.dto.ResourceDto;
import com.example.fieldcard.dto.ResourceListDto;
import com.example.fieldcard.service.processors.FileProcessor;
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

            // ZMIANA: Usunięto warunek filtrujący po "csv".
            String baseTitle = getBaseTitle(attributes.getTitle());
            latestFiles.putIfAbsent(baseTitle, resource);
        }

        System.out.println("--- ZNALEZIONE NAJNOWSZE PLIKI (" + latestFiles.size() + " szt.) ---");
        System.out.println("\n--- ROZPOCZYNAM POBIERANIE I PARSOWANIE PLIKÓW ---");

        for (ResourceDto resource : latestFiles.values()) {
            AttributesDto attributes = resource.getAttributes();
            String fileUrl = attributes.getLink();
            String title = attributes.getTitle();
            String baseTitle = getBaseTitle(title);
            String format = attributes.getFormat();

            System.out.println("Przetwarzanie: " + title + " (Format: " + format + ")");

            try {
                // Krok 1: Pobierz surowe bajty
                byte[] fileBytes = this.webClient.get()
                        .uri(fileUrl)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();

                // Krok 2: Sprawdź czy pobrano zawartość
                if (fileBytes != null && fileBytes.length > 0) {
                    System.out.println("  Pobrano " + fileBytes.length + " bajtów.");

                    boolean processed = false;
                    for (FileProcessor processor : this.fileProcessors) {
                        if (processor.supports(baseTitle)) {
                            // Krok 3: Przekaż surowe bajty (fileBytes) do procesora
                            processor.process(fileBytes);
                            processed = true;
                            break;
                        }
                    }
                    if (!processed) {
                        System.out.println("  Nie znaleziono procesora dla: " + baseTitle);
                    }

                } else {
                    System.out.println("  Nie udało się pobrać zawartości (pusty plik).");
                }

            } catch (Exception e) {
                System.out.println("  Błąd podczas pobierania pliku " + title + ": " + e.getMessage());
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