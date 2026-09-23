package com.seyahood.controller;

import com.seyahood.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entries")
@RequiredArgsConstructor
public class PublicLocationController {
    private final EntryService entryService;

    @GetMapping("/public-locations")
    public ResponseEntity<List<Map<String, Object>>> getPublicLocations() {
        return ResponseEntity.ok(entryService.getPublicLocations());
    }
}
