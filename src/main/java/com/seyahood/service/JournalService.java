package com.seyahood.service;

import com.seyahood.model.*;
import com.seyahood.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalService {
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final CloudinaryService cloudinaryService;
    private final UserJournalSaveRepository userJournalSaveRepository;

    public Journal create(String username, String title, String visibility) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Ajanda limit kontrolü
        long journalCount = journalRepository.countByUser(user);
        if (journalCount >= user.getJournalLimit()) {
            throw new RuntimeException("JOURNAL_LIMIT_REACHED");
        }

        Journal journal = new Journal();
        journal.setUser(user);
        journal.setTitle(title);
        if (visibility != null) {
            journal.setVisibility(Journal.Visibility.valueOf(visibility));
        }
        return journalRepository.save(journal);
    }

    public List<Journal> getMyJournals(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return journalRepository.findByUserId(user.getId());
    }

    public List<Journal> getPublicJournals(String search, String sortBy) {
        List<Journal> journals;

        if (search != null && !search.isEmpty()) {
            journals = journalRepository
                    .findByVisibilityAndTitleContainingIgnoreCaseOrVisibilityAndUserUsernameContainingIgnoreCase(
                            Journal.Visibility.PUBLIC, search,
                            Journal.Visibility.PUBLIC, search
                    );
        } else if ("popular".equals(sortBy)) {
            journals = journalRepository.findByVisibilityOrderBySaveCountDesc(Journal.Visibility.PUBLIC);
        } else if ("most_views".equals(sortBy)) {
            journals = journalRepository.findByVisibilityOrderByViewCountDesc(Journal.Visibility.PUBLIC);
        } else {
            journals = journalRepository.findByVisibilityOrderByCreatedAtDesc(Journal.Visibility.PUBLIC);
        }

        return journals;
    }

    public List<Journal> getSavedJournals(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        List<UserJournalSave> saves = userJournalSaveRepository.findByUser(user);
        return saves.stream().map(UserJournalSave::getJournal).toList();
    }

    public boolean toggleSave(String username, Long journalId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));

        if (userJournalSaveRepository.existsByUserAndJournal(user, journal)) {
            userJournalSaveRepository.findByUserAndJournal(user, journal)
                    .ifPresent(userJournalSaveRepository::delete);
            journal.setSaveCount(Math.max(0, journal.getSaveCount() - 1));
            journalRepository.save(journal);
            return false;
        } else {
            UserJournalSave save = new UserJournalSave();
            save.setUser(user);
            save.setJournal(journal);
            userJournalSaveRepository.save(save);
            journal.setSaveCount(journal.getSaveCount() + 1);
            journalRepository.save(journal);
            return true;
        }
    }

    public boolean isSaved(String username, Long journalId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));
        return userJournalSaveRepository.existsByUserAndJournal(user, journal);
    }

    public Journal incrementView(Long journalId) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));
        journal.setViewCount(journal.getViewCount() + 1);
        return journalRepository.save(journal);
    }

    public Journal uploadCover(Long id, String username, MultipartFile file) throws IOException {
        Journal journal = journalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));

        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu ajandayı düzenleme yetkiniz yok");
        }

        String imageUrl = cloudinaryService.upload(file);
        journal.setCoverImageUrl(imageUrl);
        return journalRepository.save(journal);
    }

    @Transactional
    public void delete(Long journalId, String username) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));

        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu ajandayı silme yetkiniz yok");
        }

        entryRepository.deleteByJournalId(journalId);
        journalRepository.delete(journal);
    }

    public Journal update(Long id, String username, Journal journalData) {
        Journal journal = journalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));

        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu ajandayı düzenleme yetkiniz yok");
        }

        if (journalData.getTitle() != null) journal.setTitle(journalData.getTitle());
        if (journalData.getVisibility() != null) journal.setVisibility(journalData.getVisibility());

        return journalRepository.save(journal);
    }
}