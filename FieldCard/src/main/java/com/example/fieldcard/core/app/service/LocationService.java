package com.example.fieldcard.core.app.service;

import com.example.fieldcard.api.LocationController.RegionDto;
import com.example.fieldcard.data.entity.AdministrativeUnit;
import com.example.fieldcard.data.repository.AdministrativeUnitRepository;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final AdministrativeUnitRepository repository;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://uldk.gugik.gov.pl")
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
                            .responseTimeout(Duration.ofSeconds(20))
                            .doOnConnected(conn ->
                                    conn.addHandlerLast(new ReadTimeoutHandler(20, TimeUnit.SECONDS))
                                            .addHandlerLast(new WriteTimeoutHandler(20, TimeUnit.SECONDS)))
            ))
            .build();

    public List<RegionDto> getRegions(String parentId) {
        String dbParentId = (parentId == null || parentId.isEmpty()) ? "ROOT" : parentId;

        List<AdministrativeUnit> cachedUnits = repository.findAllByParentId(dbParentId);
        if (!cachedUnits.isEmpty()) {
            log.debug("Pobrano z cache lokalnego dla: {}", dbParentId);
            return cachedUnits.stream()
                    .map(u -> new RegionDto(u.getId(), u.getName(), u.getType()))
                    .toList();
        }

        log.info("Cache pusty, pytam GUGiK o: {}", dbParentId);
        List<RegionDto> fromApi = fetchRegionsFromApi(parentId);

        if (!fromApi.isEmpty()) {
            try {
                List<AdministrativeUnit> entities = fromApi.stream()
                        .map(dto -> new AdministrativeUnit(dto.id(), dto.name(), dto.type(), dbParentId))
                        .toList();
                repository.saveAll(entities);
            } catch (Exception e) {
                log.warn("Błąd zapisu do cache (możliwy duplikat): {}", e.getMessage());
            }
        }

        return fromApi;
    }

    public String verifyParcel(String precinctId, String parcelNumber) {
        String fullId = precinctId + "." + parcelNumber;
        log.info("Weryfikacja działki (ULDK): {}", fullId);

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("request", "GetParcelById")
                            .queryParam("id", fullId)
                            .queryParam("result", "geom_wkt") // Prosimy o WKT (tekst)
                            .queryParam("srs", "4326")        // Układ GPS (Lat/Lon)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseWktResponse(response);

        } catch (Exception e) {
            log.error("Błąd weryfikacji działki {}: {}", fullId, e.getMessage());
            return null;
        }
    }

    private String parseWktResponse(String response) {
        if (response == null || response.isEmpty()) return null;
        String[] lines = response.split("\n");

        if (lines.length < 2 || !lines[0].trim().equals("0")) {
            return null;
        }

        String wkt = lines[1].trim();
        if (wkt.startsWith("POLYGON") || wkt.startsWith("MULTIPOLYGON")) {
            return wkt;
        }
        return null;
    }

    private List<RegionDto> fetchRegionsFromApi(String parentId) {
        try {
            String safeParentId = (parentId == null) ? "" : parentId;
            int len = safeParentId.length();

            String requestType;
            String resultParam;
            String hardcodedType;

            if (len == 0) {
                requestType = "GetVoivodeshipById";
                resultParam = "id,voivodeship";
                hardcodedType = "województwo";
            } else if (len == 2) {
                requestType = "GetCountyById";
                resultParam = "id,county";
                hardcodedType = "powiat";
            } else if (len == 4) {
                requestType = "GetCommuneById";
                resultParam = "id,commune";
                hardcodedType = "gmina";
            } else {
                requestType = "GetRegionById"; // Obręby
                resultParam = "id,region";
                hardcodedType = "obręb";
            }

            String response = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/")
                                .queryParam("request", requestType)
                                .queryParam("result", resultParam);
                        if (!safeParentId.isEmpty()) builder.queryParam("id", safeParentId);
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseUldkResponse(response, hardcodedType);

        } catch (Exception e) {
            log.error("Błąd API GUGiK (Regions): {}", e.getMessage());
            return List.of();
        }
    }

    private List<RegionDto> parseUldkResponse(String response, String hardcodedType) {
        List<RegionDto> regions = new ArrayList<>();
        if (response == null || response.isEmpty() || response.startsWith("-1")) return regions;

        String[] lines = response.split("\n");
        int startIndex = (lines.length > 0 && lines[0].matches("^\\d.*") && lines[0].length() < 5) ? 1 : 0;

        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String id = "";
            String name = "";

            if (line.contains("|")) {
                String[] parts = line.split("\\|");
                if (parts.length > 0) id = parts[0];
                if (parts.length > 1) name = parts[1];
            } else {
                String[] parts = line.split(" ", 2);
                if (parts.length >= 2) {
                    id = parts[0];
                    name = parts[1].trim();
                }
            }

            if (!id.isEmpty()) {
                regions.add(new RegionDto(id, capitalize(name), hardcodedType));
            }
        }
        return regions;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}