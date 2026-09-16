package com.memorylane.memorylane.controller;

import com.memorylane.memorylane.model.PromoCode;
import com.memorylane.memorylane.model.User;
import com.memorylane.memorylane.service.JwtService;
import com.memorylane.memorylane.service.PromoCodeService;
import com.memorylane.memorylane.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PromoCodeService promoCodeService;

    @Value("${app.admin-key:}")
    private String adminKey;

    private boolean isAdmin(String providedKey) {
        return adminKey != null && !adminKey.isBlank() && adminKey.equals(providedKey);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.getProfile(username);

        return ResponseEntity.ok(Map.of(
                "plan", user.getSubscriptionPlan(),
                "isPlus", user.isPlus(),
                "isPro", user.isPro(),
                "journalLimit", user.getJournalLimit(),
                "pageLimit", user.getPageLimit()
        ));
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> event =
                    (Map<String, Object>) payload.get("event");
            String type = (String) event.get("type");
            String appUserId = (String) event.get("app_user_id");

            User user;
            try {
                user = userService.findByRevenueCatCustomerId(appUserId);
            } catch (RuntimeException e) {
                return ResponseEntity.ok().build();
            }

            switch (type) {
                case "INITIAL_PURCHASE":
                case "RENEWAL":
                case "PRODUCT_CHANGE": {
                    String productId = (String) event.get("product_id");
                    User.SubscriptionPlan plan =
                            productId.contains("pro")
                                    ? User.SubscriptionPlan.PRO
                                    : User.SubscriptionPlan.PLUS;
                    user.setSubscriptionPlan(plan);
                    user.setSubscriptionExpiresAt(
                            LocalDateTime.now().plusMonths(1));
                    break;
                }
                case "EXPIRATION":
                case "CANCELLATION": {
                    user.setSubscriptionPlan(User.SubscriptionPlan.FREE);
                    user.setSubscriptionExpiresAt(null);
                    break;
                }
            }
            userService.save(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register-customer")
    public ResponseEntity<?> registerCustomer(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.getProfile(username);
        user.setRevenueCatCustomerId(body.get("customerId"));
        userService.save(user);
        return ResponseEntity.ok(Map.of("message", "Customer ID registered"));
    }

    @PostMapping("/redeem-code")
    public ResponseEntity<?> redeemCode(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CODE_REQUIRED"));
        }
        try {
            User user = promoCodeService.redeemCode(username, code);
            return ResponseEntity.ok(Map.of(
                    "message", "Kod başarıyla kullanıldı",
                    "plan", user.getSubscriptionPlan(),
                    "subscriptionExpiresAt", user.getSubscriptionExpiresAt()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/codes")
    public ResponseEntity<?> createCode(
            @RequestHeader("X-Admin-Key") String providedKey,
            @RequestBody Map<String, Object> body) {
        if (!isAdmin(providedKey)) {
            return ResponseEntity.status(403).body(Map.of("error", "FORBIDDEN"));
        }
        try {
            String code = (String) body.get("code");
            User.SubscriptionPlan plan =
                    User.SubscriptionPlan.valueOf(((String) body.get("plan")).toUpperCase());
            int durationDays = ((Number) body.get("durationDays")).intValue();
            int maxRedemptions = body.get("maxRedemptions") != null
                    ? ((Number) body.get("maxRedemptions")).intValue() : 1;
            String note = (String) body.get("note");
            LocalDateTime expiresAt = body.get("expiresAt") != null
                    ? LocalDateTime.parse((String) body.get("expiresAt")) : null;

            PromoCode promoCode = promoCodeService.createCode(
                    code, plan, durationDays, maxRedemptions, expiresAt, note);
            return ResponseEntity.ok(promoCode);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/codes")
    public ResponseEntity<?> listCodes(@RequestHeader("X-Admin-Key") String providedKey) {
        if (!isAdmin(providedKey)) {
            return ResponseEntity.status(403).body(Map.of("error", "FORBIDDEN"));
        }
        List<PromoCode> codes = promoCodeService.listCodes();
        return ResponseEntity.ok(codes);
    }

    @PostMapping("/admin/codes/{id}/deactivate")
    public ResponseEntity<?> deactivateCode(
            @RequestHeader("X-Admin-Key") String providedKey,
            @PathVariable Long id) {
        if (!isAdmin(providedKey)) {
            return ResponseEntity.status(403).body(Map.of("error", "FORBIDDEN"));
        }
        promoCodeService.setActive(id, false);
        return ResponseEntity.ok(Map.of("message", "Kod devre dışı bırakıldı"));
    }
}