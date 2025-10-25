package com.example.fieldcard.service.processors;

public interface FileProcessor {
    boolean supports(String baseTitle);
    void process(String fileContent);
}
