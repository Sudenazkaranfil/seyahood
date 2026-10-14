package com.seyahood.service;

import com.seyahood.model.DeviceToken;
import com.seyahood.model.Journal;
import com.seyahood.model.Notification;
import com.seyahood.model.User;
import com.seyahood.repository.DeviceTokenRepository;
import com.seyahood.repository.NotificationRepository;
import com.seyahood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationService pushNotificationService;

    public void notifyFollow(User actor, User recipient) {
        if (actor.getId().equals(recipient.getId())) return;
        create(recipient, actor, Notification.Type.FOLLOW, null,
                displayName(actor) + " seni takip etmeye başladı");
    }

    public void notifyJournalSave(User actor, User recipient, Journal journal) {
        if (actor.getId().equals(recipient.getId())) return;
        create(recipient, actor, Notification.Type.JOURNAL_SAVE, journal,
                displayName(actor) + " \"" + journal.getTitle() + "\" ajandanı kaydetti");
    }

    private void create(User recipient, User actor, Notification.Type type, Journal journal, String body) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(type);
        notification.setJournal(journal);
        notificationRepository.save(notification);

        pushNotificationService.sendToUser(recipient, "Seyahood", body);
    }

    private String displayName(User user) {
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            return user.getFirstName();
        }
        return user.getUsername();
    }

    public List<Notification> getForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    public void markRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı"));
        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new RuntimeException("Bu bildirime erişim yetkiniz yok");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        notificationRepository.markAllRead(user);
    }

    public void registerDeviceToken(String username, String token, String platform) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        deviceTokenRepository.findByToken(token).ifPresentOrElse(existing -> {
            existing.setUser(user);
            existing.setPlatform(platform);
            deviceTokenRepository.save(existing);
        }, () -> {
            DeviceToken deviceToken = new DeviceToken();
            deviceToken.setUser(user);
            deviceToken.setToken(token);
            deviceToken.setPlatform(platform);
            deviceTokenRepository.save(deviceToken);
        });
    }

    public void unregisterDeviceToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
