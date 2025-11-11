package com.example.fieldcard.service.processors;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset; // <-- Dodaj ten import

public abstract class AbstractCsvProcessor implements  FileProcessor {

    /**
     * KROK 1: Implementujemy nową metodę z interfejsu FileProcessor.
     * To jest "mostek".
     */
    @Override
    public void process(byte[] fileContent) {
        // KROK 2: Konwertujemy bajty na String, tak jak potrzebują tego procesory CSV.
        String contentString = new String(fileContent, Charset.forName("Windows-1250"));

        // KROK 3: Wywołujemy starą metodę process(String), którą nadpisują
        // Twoje klasy (np. ApplicationGroupProcessor).
        process(contentString);
    }

    /**
     * KROK 4: Zachowujemy oryginalną metodę process(String).
     * Twoje klasy (CropProcessor, PestProcessor itd.) nadal ją nadpisują
     * i nadal mogą wywoływać super.process(fileContent), aby uruchomić ten parser.
     */
    public void process(String fileContent) {
        try (Reader reader = new StringReader(fileContent);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withSkipLines(1)
                     .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                     .build()) {

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                processRow(nextRecord);
            }

        } catch (Exception e) {
            System.out.println("    [AbstractCsvProcessor] Błąd podczas parsowania CSV: " + e.getMessage());
        }
    }


    protected abstract void processRow(String[] nextRecord);
    @Override
    public abstract boolean supports(String baseTitle);
}