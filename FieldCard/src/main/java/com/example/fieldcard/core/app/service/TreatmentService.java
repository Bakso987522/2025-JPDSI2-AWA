package com.example.fieldcard.core.app.service;

import com.example.fieldcard.dto.response.*;
import com.example.fieldcard.dto.request.*;
import com.example.fieldcard.data.entity.*;
import com.example.fieldcard.data.repository.FieldRepository;
import com.example.fieldcard.data.repository.TreatmentRepository;
import com.example.fieldcard.dto.request.TreatmentItemDto;
import com.example.fieldcard.dto.request.TreatmentRequestDto;
import com.example.fieldcard.dto.response.TreatmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final FieldRepository fieldRepository;

    public void deleteTreatment(Long treatmentId, String userEmail) {
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new RuntimeException("Zabieg nie istnieje"));

        String ownerEmail = treatment.getField().getUser().getEmail();

        if (!ownerEmail.equals(userEmail)) {
            throw new SecurityException("Nie masz uprawnień do usunięcia tego zabiegu");
        }

        treatmentRepository.delete(treatment);
    }
    @Transactional
    public void addTreatment(TreatmentRequestDto request, String userEmail) {
        Field field = fieldRepository.findById(request.getFieldId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pola"));

        if (!field.getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Nie masz uprawnień do tego pola");
        }

        Treatment treatment = Treatment.builder()
                .date(request.getDate())
                .description(request.getDescription())
                .field(field)
                .build();

        if (request.getItems() != null) {
            for (TreatmentItemDto itemDto : request.getItems()) {
                TreatmentItem item = TreatmentItem.builder()
                        .productName(itemDto.getProductName())
                        .activeSubstance(itemDto.getActiveSubstance())
                        .targetPest(itemDto.getTargetPest())
                        .dose(itemDto.getDose())
                        .isOffLabel(itemDto.isOffLabel())
                        .build();

                treatment.addItem(item);
            }
        }

        treatmentRepository.save(treatment);
    }

    @Transactional(readOnly = true)
    public List<TreatmentDto> getUserTreatments(String userEmail) {
        return treatmentRepository.findAllByUserEmail(userEmail).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private TreatmentDto mapToResponse(Treatment t) {
        return TreatmentDto.builder()
                .id(t.getId())
                .date(t.getDate())
                .fieldName(t.getField().getName())
                .items(t.getItems().stream().map(item -> {
                    TreatmentItemDto itemDto = new TreatmentItemDto();
                    itemDto.setProductName(item.getProductName());
                    itemDto.setActiveSubstance(item.getActiveSubstance());
                    itemDto.setTargetPest(item.getTargetPest());
                    itemDto.setDose(item.getDose());
                    itemDto.setOffLabel(item.isOffLabel());
                    return itemDto;
                }).collect(Collectors.toList()))
                .build();
    }
}