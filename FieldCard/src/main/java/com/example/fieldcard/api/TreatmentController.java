package com.example.fieldcard.api;

import com.example.fieldcard.dto.request.TreatmentRequestDto;
import com.example.fieldcard.dto.response.TreatmentDto;
import com.example.fieldcard.core.app.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
public class TreatmentController {

    private final TreatmentService treatmentService;

    @GetMapping
    public ResponseEntity<List<TreatmentDto>> getAll(Authentication auth) {
        return ResponseEntity.ok(treatmentService.getUserTreatments(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody TreatmentRequestDto request, Authentication auth) {
        treatmentService.addTreatment(request, auth.getName());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTreatment(@PathVariable Long id, Authentication authentication) {
        treatmentService.deleteTreatment(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}