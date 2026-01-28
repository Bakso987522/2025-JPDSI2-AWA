package com.example.fieldcard.api;

import com.example.fieldcard.core.app.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    public record RegionDto(String id, String name, String type) {}

    @GetMapping("/regions")
    public ResponseEntity<List<RegionDto>> getRegions(@RequestParam(required = false, defaultValue = "") String parentId) {
        return ResponseEntity.ok(locationService.getRegions(parentId));
    }
    @GetMapping("/verify")
    public ResponseEntity<?> verifyParcel(
            @RequestParam String precinctId,
            @RequestParam String parcelNumber
    ) {
        String geometryWkt = locationService.verifyParcel(precinctId, parcelNumber);

        if (geometryWkt != null) {
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "fullId", precinctId + "." + parcelNumber,
                    "geometry", geometryWkt
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "exists", false
            ));
        }
    }
}