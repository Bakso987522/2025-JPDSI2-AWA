package com.example.fieldcard.service.processors;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

public abstract class AbstractCsvProcessor implements  FileProcessor {


    @Override
    public void process(byte[] fileContent) {
        String contentString = new String(fileContent, Charset.forName("Windows-1250"));

        process(contentString);
    }


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