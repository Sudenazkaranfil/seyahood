package com.seyahood.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Ünlü/bilinen yerleri (anıt, meydan, doğal yer işareti vb.) tanır ve
 * gerçek koordinat döndürür. Google'ın ayda 1000 isteğe kadar ücretsiz
 * kotası olduğu için AI konum tahmininde Claude'dan önce denenir; sıradan
 * (ünlü olmayan) sahnelerde sonuç döndürmez.
 */
@Service
@RequiredArgsConstructor
public class GoogleVisionService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${google.vision.api.key:}")
    private String apiKey;

    public record LandmarkResult(String name, Double lat, Double lng) {}

    @SuppressWarnings("unchecked")
    public LandmarkResult detectLandmark(byte[] imageBytes) throws Exception {
        if (apiKey == null || apiKey.isBlank()) return null;

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        Map<String, Object> requestBody = Map.of(
                "requests", List.of(Map.of(
                        "image", Map.of("content", base64Image),
                        "features", List.of(Map.of(
                                "type", "LANDMARK_DETECTION", "maxResults", 1))
                ))
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://vision.googleapis.com/v1/images:annotate?key=" + apiKey))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Vision API error: " + response.statusCode() + " " + response.body());
        }

        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> responses = (List<Map<String, Object>>) responseMap.get("responses");
        if (responses == null || responses.isEmpty()) return null;

        List<Map<String, Object>> landmarks =
                (List<Map<String, Object>>) responses.get(0).get("landmarkAnnotations");
        if (landmarks == null || landmarks.isEmpty()) return null;

        Map<String, Object> landmark = landmarks.get(0);
        String name = (String) landmark.get("description");

        Double lat = null;
        Double lng = null;
        List<Map<String, Object>> locations = (List<Map<String, Object>>) landmark.get("locations");
        if (locations != null && !locations.isEmpty()) {
            Map<String, Object> latLng = (Map<String, Object>) locations.get(0).get("latLng");
            if (latLng != null) {
                lat = ((Number) latLng.get("latitude")).doubleValue();
                lng = ((Number) latLng.get("longitude")).doubleValue();
            }
        }
        return new LandmarkResult(name, lat, lng);
    }
}
