package com.seyahood.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.seyahood.model.DeviceToken;
import com.seyahood.repository.DeviceTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Takip/kaydetme gibi olaylarda cihazlara push bildirimi gönderir.
 * FIREBASE_SERVICE_ACCOUNT_JSON ortam değişkeni ayarlanana kadar
 * sessizce no-op çalışır (bildirim uygulama içinde yine kaydedilir,
 * sadece push gönderilmez) — Anthropic/Google Vision anahtarlarıyla
 * aynı "key yoksa devre dışı" deseni.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    private boolean available = false;

    @PostConstruct
    public void init() {
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            log.info("FIREBASE_SERVICE_ACCOUNT_JSON ayarlı değil, push bildirimleri devre dışı.");
            return;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
            }
            available = true;
        } catch (Exception e) {
            log.warn("Firebase başlatılamadı, push bildirimleri devre dışı: {}", e.getMessage());
        }
    }

    public void sendToUser(com.seyahood.model.User user, String title, String body) {
        if (!available) return;

        List<DeviceToken> tokens = deviceTokenRepository.findByUser(user);
        for (DeviceToken deviceToken : tokens) {
            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            try {
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED) {
                    deviceTokenRepository.delete(deviceToken);
                } else {
                    log.warn("Push gönderilemedi: {}", e.getMessage());
                }
            }
        }
    }
}
