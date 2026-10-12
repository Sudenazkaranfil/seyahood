package com.memorylane.memorylane.service;

import com.memorylane.memorylane.model.Entry;
import com.memorylane.memorylane.model.Journal;
import com.memorylane.memorylane.repository.EntryRepository;
import com.memorylane.memorylane.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {
    private final EntryRepository entryRepository;
    private final JournalRepository journalRepository;

    public Entry create(Long journalId, String username, Entry entryData) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));

        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu ajandaya erişim yetkiniz yok");
        }

        // Sayfa limit kontrolü
        long entryCount = entryRepository.countByJournal(journal);
        int pageLimit = journal.getUser().getPageLimit();
        if (entryCount >= pageLimit) {
            throw new RuntimeException("PAGE_LIMIT_REACHED");
        }

        entryData.setJournal(journal);
        return entryRepository.save(entryData);
    }

    public List<Entry> getEntries(Long journalId, String username) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Ajanda bulunamadı"));

        if (journal.getVisibility() == Journal.Visibility.PUBLIC) {
            return entryRepository.findByJournalIdOrderByCreatedAtAsc(journalId);
        }

        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu ajandaya erişim yetkiniz yok");
        }

        return entryRepository.findByJournalIdOrderByCreatedAtAsc(journalId);
    }

    public void delete(Long entryId, String username) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Giriş bulunamadı"));

        if (!entry.getJournal().getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu girişi silme yetkiniz yok");
        }

        entryRepository.delete(entry);
    }

    public Entry addPhotoToEntry(Long entryId, String username, String imageUrl) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Giriş bulunamadı"));

        if(!entry.getJournal().getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu girişe erişim yetkiniz yok");
        }

        entry.getPhotoUrls().add(imageUrl);
        return entryRepository.save(entry);
    }

    public Entry update(Long journalId, Long entryId, String username, Entry entryData) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Giriş bulunamadı"));

        if (!entry.getJournal().getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu girişe erişim yetkiniz yok");
        }

        if (entryData.getTextContent() != null) entry.setTextContent(entryData.getTextContent());
        if (entryData.getLocationName() != null) entry.setLocationName(entryData.getLocationName());
        if (entryData.getLat() != null) entry.setLat(entryData.getLat());
        if (entryData.getLng() != null) entry.setLng(entryData.getLng());
        if (entryData.getCanvasData() != null) entry.setCanvasData(entryData.getCanvasData());
        if (entryData.getMood() != null) entry.setMood(entryData.getMood());

        return entryRepository.save(entry);
    }

    public List<Map<String, Object>> getPublicLocations() {
        return entryRepository.findPublicLocations().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", entry.getId());
                    map.put("locationName", entry.getLocationName());
                    map.put("lat", entry.getLat());
                    map.put("lng", entry.getLng());
                    map.put("textContent", entry.getTextContent());
                    map.put("journalId", entry.getJournal().getId());
                    map.put("journalTitle", entry.getJournal().getTitle());
                    map.put("username", entry.getJournal().getUser().getUsername());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
