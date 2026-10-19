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

@Service
@RequiredArgsConstructor
public class LocationEstimationService {

    private static final String ANTHROPIC_MODEL = "claude-sonnet-5";
    private static final String PROMPT =
            "Bu fotoğraftaki yeri tahmin et. Sadece yer adını kısa ve net şekilde " +
            "yaz (örnek: 'Kapadokya, Türkiye' veya 'Eyfel Kulesi, Paris'). Emin " +
            "olamazsan genel bir tahmin yap (örnek: 'Sahil kenti' veya 'Dağlık " +
            "bölge'). Başka açıklama ekleme, sadece yer adını yaz.";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    @SuppressWarnings("unchecked")
    public String estimateLocation(byte[] imageBytes, String contentType) throws Exception {
        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            throw new IllegalStateException("Anthropic API key not configured");
        }
        String mediaType = (contentType != null && contentType.startsWith("image/"))
                ? contentType : "image/jpeg";
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> requestBody = Map.of(
                "model", ANTHROPIC_MODEL,
                "max_tokens", 60,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "image", "source", Map.of(
                                        "type", "base64",
                                        "media_type", mediaType,
                                        "data", base64Image)),
                                Map.of("type", "text", "text", PROMPT)
                        )
                ))
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("x-api-key", anthropicApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Anthropic API error: " + response.statusCode() + " " + response.body());
        }

        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");
        return ((String) content.get(0).get("text")).trim();
    }
}
