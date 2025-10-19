package com.example.fieldcard.service;

import com.example.fieldcard.dto.ResourceListDto;
import com.example.fieldcard.dto.ResourceDto;
import com.example.fieldcard.dto.AttributesDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataSyncService {
    private final String API_URL = "https://api.dane.gov.pl/1.4/datasets/550,wyszukiwarka-srodkow-ochrony-roslin-1/resources?sort=-data_date&per_page=50";

    private final WebClient webClient;
    @Autowired
    public  DataSyncService(WebClient webClient) {
        this.webClient = webClient;
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
            String format = attributes.getFormat();
            if (attributes != null && ("csv".equals(format) || "xlsx".equals(format))) {
                String baseTitle = getBaseTitle(attributes.getTitle());
                if (!latestFiles.containsKey(baseTitle)) {
                    latestFiles.put(baseTitle, resource);
                }
            }
        }

        System.out.println("--- ZNALEZIONE NAJNOWSZE PLIKI (" + latestFiles.size() + " szt.) ---");

        System.out.println("\n--- ROZPOCZYNAM POBIERANIE PLIKÓW ---");

        for (ResourceDto resource : latestFiles.values()) {
            AttributesDto attributes = resource.getAttributes();
            String fileUrl = attributes.getLink();
            String title = attributes.getTitle();

            System.out.println("Pobieranie: " + title + " z " + fileUrl);

            try {
                String fileContent = this.webClient.get()
                        .uri(fileUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (fileContent != null) {
                    System.out.println("  Pobrano " + fileContent.length() + " znaków.");
                    System.out.println("  Początek pliku: " + fileContent.substring(0, Math.min(fileContent.length(), 200)) + "...");
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
        return fullTitle.split(" - ")[0].trim();
    }
    }

