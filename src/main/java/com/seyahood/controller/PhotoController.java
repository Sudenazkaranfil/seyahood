package com.seyahood.controller;

import com.seyahood.service.CloudinaryService;
import com.seyahood.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/entries/{entryId}/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final CloudinaryService cloudinaryService;
    private final EntryService entryService;

    @PostMapping
    public ResponseEntity<?> uploadPhoto(
            @AuthenticationPrincipal String username,
            @PathVariable Long entryId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = cloudinaryService.upload(file);
        entryService.addPhotoToEntry(entryId, username, imageUrl);
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }
}
