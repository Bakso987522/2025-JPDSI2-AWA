package com.example.fieldcard.core.importer.processor;

public interface FileProcessor {
    boolean supports(String baseTitle);
    void process(byte[] fileContent); 
}