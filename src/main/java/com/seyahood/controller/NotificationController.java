package com.seyahood.controller;

import com.seyahood.model.Notification;
import com.seyahood.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(notificationService.getForUser(username));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(username)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        notificationService.markRead(id, username);
        return ResponseEntity.ok(Map.of("read", true));
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllRead(@AuthenticationPrincipal String username) {
        notificationService.markAllRead(username);
        return ResponseEntity.ok(Map.of("read", true));
    }

    @PostMapping("/device-token")
    public ResponseEntity<?> registerDeviceToken(
            @AuthenticationPrincipal String username,
            @RequestBody Map<String, String> body) {
        notificationService.registerDeviceToken(username, body.get("token"), body.get("platform"));
        return ResponseEntity.ok(Map.of("registered", true));
    }

    @DeleteMapping("/device-token")
    public ResponseEntity<?> unregisterDeviceToken(@RequestBody Map<String, String> body) {
        notificationService.unregisterDeviceToken(body.get("token"));
        return ResponseEntity.ok(Map.of("unregistered", true));
    }
}
