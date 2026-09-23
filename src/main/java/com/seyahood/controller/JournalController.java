package com.seyahood.controller;

import com.seyahood.model.Journal;
import com.seyahood.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal String username,
            @RequestBody Map<String, String> body) {
        try {
            Journal journal = journalService.create(username, body.get("title"), body.get("visibility"));
            return ResponseEntity.ok(journal);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("JOURNAL_LIMIT_REACHED")) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "JOURNAL_LIMIT_REACHED",
                        "message", "Ajanda limitine ulaştınız"
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Journal>> getMyJournals(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(journalService.getMyJournals(username));
    }

    @GetMapping("/public")
    public ResponseEntity<List<Journal>> getPublicJournals(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(journalService.getPublicJournals(search, sortBy));
    }

    @GetMapping("/saved")
    public ResponseEntity<List<Journal>> getSavedJournals(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(journalService.getSavedJournals(username));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<?> toggleSave(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        boolean saved = journalService.toggleSave(username, id);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @GetMapping("/{id}/saved")
    public ResponseEntity<?> isSaved(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        boolean saved = journalService.isSaved(username, id);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Journal> incrementView(@PathVariable Long id) {
        return ResponseEntity.ok(journalService.incrementView(id));
    }

    @PostMapping("/{id}/cover")
    public ResponseEntity<?> uploadCover(
            @AuthenticationPrincipal String username,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        Journal journal = journalService.uploadCover(id, username, file);
        return ResponseEntity.ok(Map.of("coverImageUrl", journal.getCoverImageUrl()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        journalService.delete(id, username);
        return ResponseEntity.ok("Ajanda silindi");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Journal> update(
            @AuthenticationPrincipal String username,
            @PathVariable Long id,
            @RequestBody Journal journalData) {
        Journal journal = journalService.update(id, username, journalData);
        return ResponseEntity.ok(journal);
    }
}