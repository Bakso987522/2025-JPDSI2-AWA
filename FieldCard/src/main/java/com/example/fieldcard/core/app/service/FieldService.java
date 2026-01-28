package com.example.fieldcard.core.app.service;

import com.example.fieldcard.data.entity.Field;
import com.example.fieldcard.data.entity.Parcel;
import com.example.fieldcard.data.entity.User;
import com.example.fieldcard.data.repository.FieldRepository;
import com.example.fieldcard.data.repository.UserRepository;
import com.example.fieldcard.dto.request.CreateFieldDto;
import jakarta.persistence.EntityNotFoundException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection; // Import dodany
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class FieldService {

    private static final Logger log = LoggerFactory.getLogger(FieldService.class);

    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;
    private final WKTReader wktReader;

    public FieldService(FieldRepository fieldRepository, UserRepository userRepository) {
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://uldk.gugik.gov.pl")
                .build();
        this.wktReader = new WKTReader();
    }

    @Transactional
    public void createField(String userEmail, CreateFieldDto dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Field field = new Field();
        field.setUser(user);
        field.setName(dto.name());
        field.setArea(dto.area());
        field.setDescription(dto.description());

        List<Geometry> geometriesToMerge = new ArrayList<>();

        for (String parcelId : dto.parcelIds()) {
            Parcel parcel = new Parcel();
            parcel.setParcelNumber(extractParcelNumber(parcelId));
            parcel.setPrecinct(extractPrecinct(parcelId));

            try {
                Geometry g = fetchGeometryFromUldk(parcelId);

                if (g instanceof Polygon) {
                    parcel.setGeometry((Polygon) g);
                } else if (g instanceof GeometryCollection && g.getNumGeometries() > 0) {
                    Geometry firstGeom = g.getGeometryN(0);
                    if (firstGeom instanceof Polygon) {
                        parcel.setGeometry((Polygon) firstGeom);
                    }
                }

                if (parcel.getGeometry() != null) {
                    geometriesToMerge.add(parcel.getGeometry());
                }

            } catch (Exception e) {

                log.warn("Nie udało się pobrać geometrii dla działki: {}. Zapisuję bez mapy. Błąd: {}", parcelId, e.getMessage());
                parcel.setGeometry(null);
            }

            field.addParcel(parcel);
        }

        if (!geometriesToMerge.isEmpty()) {
            try {
                Geometry union = CascadedPolygonUnion.union(geometriesToMerge);
                field.setBorder(union);
            } catch (Exception e) {
                log.error("Błąd podczas scalania geometrii pola", e);
            }
        }

        fieldRepository.save(field);
    }

    private Geometry fetchGeometryFromUldk(String parcelId) throws Exception {
        if (parcelId == null || !parcelId.contains(".")) {
            throw new IllegalArgumentException("Niepoprawny format ID TERYT: " + parcelId);
        }

        String response = webClient.get()
                .uri(uri -> uri.path("/")
                        .queryParam("request", "GetParcelById")
                        .queryParam("id", parcelId)
                        .queryParam("result", "geom_wkt")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response != null && response.startsWith("0")) {
            String wkt = response.substring(2).trim();
            return wktReader.read(wkt);
        }
        throw new RuntimeException("ULDK zwrócił błąd lub brak danych: " + response);
    }

    private String extractParcelNumber(String fullId) {
        if (fullId == null) return "";
        return fullId.contains(".") ? fullId.substring(fullId.lastIndexOf(".") + 1) : fullId;
    }

    private String extractPrecinct(String fullId) {
        if (fullId == null) return "";
        return fullId.contains(".") ? fullId.substring(0, fullId.lastIndexOf(".")) : "";
    }
}