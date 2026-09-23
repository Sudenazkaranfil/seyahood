package com.seyahood.controller;

import com.seyahood.model.Entry;
import com.seyahood.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/journals/{journalId}/entries")
@RequiredArgsConstructor
public class EntryController {
    private final EntryService entryService;

    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal String username,
            @PathVariable Long journalId,
            @RequestBody Entry entry) {
        try {
            return ResponseEntity.ok(entryService.create(journalId, username, entry));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("PAGE_LIMIT_REACHED")) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "PAGE_LIMIT_REACHED",
                        "message", "Bu ajandadaki sayfa limitine ulaştınız"
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Entry>> getEntries(
            @AuthenticationPrincipal String username,
            @PathVariable Long journalId) {
        return ResponseEntity.ok(entryService.getEntries(journalId, username));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<String> delete(
            @AuthenticationPrincipal String username,
            @PathVariable Long entryId) {
        entryService.delete(entryId, username);
        return ResponseEntity.ok("Giriş silindi");
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<Entry> update(
            @AuthenticationPrincipal String username,
            @PathVariable Long journalId,
            @PathVariable Long entryId,
            @RequestBody Entry entryData) {
        Entry entry = entryService.update(journalId, entryId, username, entryData);
        return ResponseEntity.ok(entry);
    }
}
