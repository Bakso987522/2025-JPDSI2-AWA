package com.example.fieldcard.api;

import com.example.fieldcard.core.app.service.FieldService;
import com.example.fieldcard.data.entity.Field;
import com.example.fieldcard.data.entity.User;
import com.example.fieldcard.data.repository.FieldRepository;
import com.example.fieldcard.data.repository.UserRepository;
import com.example.fieldcard.dto.request.CreateFieldDto;
import com.example.fieldcard.dto.response.FieldDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fields")
public class FieldController {

    private final FieldService fieldService;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;

    public FieldController(FieldService fieldService, FieldRepository fieldRepository, UserRepository userRepository) {
        this.fieldService = fieldService;
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createField(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateFieldDto dto
    ) {
        fieldService.createField(userDetails.getUsername(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<FieldDto>> getMyFields(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<FieldDto> fieldDtos = fieldRepository.findAllByUser(user).stream()
                .map(f -> new FieldDto(
                        f.getId(),
                        f.getName(),
                        f.getArea(),
                        f.getDescription(),
                        f.getParcels().stream()
                                .map(parcel -> parcel.getParcelNumber())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(fieldDtos);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteField(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Field not found"));

        if (!field.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        fieldRepository.delete(field);
        return ResponseEntity.ok().build();
    }
}