package com.example.fieldcard.service;

import com.example.fieldcard.dto.AttributesDto;
import com.example.fieldcard.dto.ResourceDto;
import com.example.fieldcard.dto.ResourceListDto;
import com.example.fieldcard.entity.Crop;
import com.example.fieldcard.repository.CropRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Service
public class DataSyncService {

    private final String API_URL = "https://api.dane.gov.pl/1.4/datasets/550,wyszukiwarka-srodkow-ochrony-roslin-1/resources?sort=-data_date&per_page=50";
    private final WebClient webClient;
    private final CropRepository cropRepository;

    @Autowired
    public DataSyncService(WebClient webClient, CropRepository cropRepository) {
        this.webClient = webClient;
        this.cropRepository = cropRepository;
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

            String format = attributes.getFormat();
            if (("csv".equals(format) || "xlsx".equals(format))) {
                String baseTitle = getBaseTitle(attributes.getTitle());
                latestFiles.putIfAbsent(baseTitle, resource);
            }
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
                String fileContent = this.webClient.get()
                        .uri(fileUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(content -> new String(content.getBytes(StandardCharsets.ISO_8859_1), Charset.forName("Windows-1250")))
                        .block();

                if (fileContent != null && !fileContent.isEmpty()) {
                    System.out.println("  Pobrano " + fileContent.length() + " znaków.");

                    if ("csv".equals(format)) {
                        if ("słownik uprawy".equals(baseTitle)) {
                            System.out.println("    Parsowanie 'słownik uprawy'...");
                            List<String> cropNames = new ArrayList<>();
                            try (Reader reader = new StringReader(fileContent);
                                 CSVReader csvReader = new CSVReaderBuilder(reader)
                                         .withSkipLines(1)
                                         .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                                         .build()) {

                                String[] nextRecord;
                                int rowCount = 0;
                                while ((nextRecord = csvReader.readNext()) != null) {
                                    if (nextRecord.length > 0 && nextRecord[0] != null && !nextRecord[0].trim().isEmpty()) {
                                        cropNames.add(nextRecord[0].trim());
                                    }
                                }
                                System.out.println("Znaleziono:" + cropNames.size() + " nazw upraw.");

//                                  Kod do edycji na metodę przyrsotową
                                if(!cropNames.isEmpty()){
                                    List<Crop> cropsToSave = new ArrayList<>();
                                    for (String cropName : cropNames) {
                                        cropsToSave.add(new Crop(cropName));
                                    }
                                    cropRepository.deleteAllInBatch();
                                    cropRepository.saveAll(cropsToSave);
                                }
//                                  Koniec kodu do edycji

                            } catch (Exception e) {
                                System.out.println("    Błąd podczas parsowania CSV dla pliku " + title + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println("  Początek innego pliku CSV: " + fileContent.substring(0, Math.min(fileContent.length(), 200)) + "...");
                        }
                    } else if ("xlsx".equals(format)) {
                        System.out.println("  Początek pliku XLSX: " + fileContent.substring(0, Math.min(fileContent.length(), 200)) + "...");
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