package com.seyahood.service;

import com.seyahood.model.Entry;
import com.seyahood.model.Journal;
import com.seyahood.repository.EntryRepository;
import com.seyahood.repository.JournalRepository;
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
                .orElseThrow(() -> new RuntimeException("JOURNAL_NOT_FOUND"));

        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("JOURNAL_ACCESS_DENIED");
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
                .orElseThrow(() -> new RuntimeException("JOURNAL_NOT_FOUND"));

        if (journal.getVisibility() == Journal.Visibility.PUBLIC) {
            return entryRepository.findByJournalIdOrderByCreatedAtAsc(journalId);
        }

        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("JOURNAL_ACCESS_DENIED");
        }

        return entryRepository.findByJournalIdOrderByCreatedAtAsc(journalId);
    }

    public void delete(Long entryId, String username) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("ENTRY_NOT_FOUND"));

        if (!entry.getJournal().getUser().getUsername().equals(username)) {
            throw new RuntimeException("ENTRY_DELETE_DENIED");
        }

        entryRepository.delete(entry);
    }

    public Entry addPhotoToEntry(Long entryId, String username, String imageUrl) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("ENTRY_NOT_FOUND"));

        if(!entry.getJournal().getUser().getUsername().equals(username)) {
            throw new RuntimeException("ENTRY_ACCESS_DENIED");
        }

        entry.getPhotoUrls().add(imageUrl);
        return entryRepository.save(entry);
    }

    public Entry update(Long journalId, Long entryId, String username, Entry entryData) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("ENTRY_NOT_FOUND"));

        if (!entry.getJournal().getUser().getUsername().equals(username)) {
            throw new RuntimeException("ENTRY_ACCESS_DENIED");
        }

        if (entryData.getTextContent() != null) entry.setTextContent(entryData.getTextContent());
        if (entryData.getLocationName() != null) entry.setLocationName(entryData.getLocationName());
        if (entryData.getLat() != null) entry.setLat(entryData.getLat());
        if (entryData.getLng() != null) entry.setLng(entryData.getLng());
        if (entryData.getCanvasData() != null) entry.setCanvasData(entryData.getCanvasData());
        if (entryData.getMood() != null) entry.setMood(entryData.getMood());

        return entryRepository.save(entry);
    }

    /**
     * AI konum tahmininin çapraz kontrol için kullanacağı bağlam: bu
     * ajandanın başlığı ve daha önce konumu belirlenmiş sayfalarının
     * özeti. Fotoğrafın kendisiyle çelişirse tahmin servisi bunu göz
     * ardı edebilir — bağlam sadece destekleyici bir ipucu.
     */
    public String buildLocationContext(Long journalId, String username) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("JOURNAL_NOT_FOUND"));
        if (!journal.getUser().getUsername().equals(username)) {
            throw new RuntimeException("JOURNAL_ACCESS_DENIED");
        }
        List<String> known = entryRepository.findByJournalIdOrderByCreatedAtAsc(journalId).stream()
                .filter(e -> e.getLocationName() != null && !e.getLocationName().isBlank())
                .map(e -> (e.getDate() != null ? e.getDate() + " - " : "") + e.getLocationName())
                .collect(Collectors.toList());
        if (known.isEmpty()) return null;
        return "Ajanda başlığı: \"" + journal.getTitle() + "\". Bu ajandadaki diğer sayfalarda "
                + "bilinen konumlar: " + String.join(", ", known) + ".";
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
