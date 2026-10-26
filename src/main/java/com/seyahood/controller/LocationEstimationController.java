package com.seyahood.controller;

import com.seyahood.model.User;
import com.seyahood.service.EntryService;
import com.seyahood.service.GoogleVisionService;
import com.seyahood.service.LocationEstimationService;
import com.seyahood.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class LocationEstimationController {

    private final GoogleVisionService googleVisionService;
    private final LocationEstimationService locationEstimationService;
    private final EntryService entryService;
    private final UserService userService;

    @PostMapping("/estimate-location")
    public ResponseEntity<?> estimateLocation(
            @AuthenticationPrincipal String username,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "journalId", required = false) Long journalId) {
        User user = userService.getProfile(username);
        if (!user.isPro()) {
            return ResponseEntity.status(403).body(Map.of("error", "PRO_REQUIRED"));
        }
        try {
            byte[] bytes = file.getBytes();

            // Önce ücretsiz Google Vision (sadece ünlü/bilinen yerler).
            GoogleVisionService.LandmarkResult landmark = null;
            try {
                landmark = googleVisionService.detectLandmark(bytes);
            } catch (Exception ignored) {
                // Vision hatası zinciri bozmasın, Claude'a düşülsün.
            }

            Map<String, Object> result = new HashMap<>();
            if (landmark != null && landmark.name() != null) {
                result.put("locationName", landmark.name());
                result.put("lat", landmark.lat());
                result.put("lng", landmark.lng());
            } else if (locationEstimationService.isConfigured()) {
                // Son çare: ücretli, genel amaçlı Claude tahmini — aynı
                // ajandanın diğer sayfalarındaki bilinen konumlar
                // destekleyici bağlam olarak veriliyor (çapraz kontrol).
                String journalContext = null;
                if (journalId != null) {
                    try {
                        journalContext = entryService.buildLocationContext(journalId, username);
                    } catch (Exception ignored) {
                        // Bağlam alınamazsa tahmin yine de denenir, context'siz.
                    }
                }
                String aiGuess = locationEstimationService.estimateLocation(
                        bytes, file.getContentType(), journalContext);
                boolean identified = aiGuess != null && !aiGuess.isBlank()
                        && !aiGuess.trim().equalsIgnoreCase("UNKNOWN");
                result.put("locationName", identified ? aiGuess.trim() : null);
                result.put("lat", null);
                result.put("lng", null);
            } else {
                // Claude katmanı yapılandırılmamış (ANTHROPIC_API_KEY yok) —
                // bu bir hata değil, sadece ücretsiz katmanlar yetersiz kaldı.
                result.put("locationName", null);
                result.put("lat", null);
                result.put("lng", null);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "ESTIMATION_FAILED"));
        }
    }
}
