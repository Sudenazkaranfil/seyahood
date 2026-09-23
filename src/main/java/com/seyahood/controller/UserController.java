package com.seyahood.controller;

import com.seyahood.model.User;
import com.seyahood.service.UserService;
import com.seyahood.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchUsers(q));
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserProfile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<?> toggleFollow(
            @AuthenticationPrincipal String currentUsername,
            @PathVariable String username) {
        boolean following = followService.toggleFollow(currentUsername, username);
        return ResponseEntity.ok(Map.of("following", following));
    }

    @GetMapping("/{username}/follow-status")
    public ResponseEntity<?> followStatus(
            @AuthenticationPrincipal String currentUsername,
            @PathVariable String username) {
        boolean following = followService.isFollowing(currentUsername, username);
        long followers = followService.getFollowerCount(username);
        long following2 = followService.getFollowingCount(username);
        return ResponseEntity.ok(Map.of(
                "following", following,
                "followerCount", followers,
                "followingCount", following2
        ));
    }
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<Map<String, Object>>> getFollowers(@PathVariable String username) {
        return ResponseEntity.ok(followService.getFollowers(username));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<List<Map<String, Object>>> getFollowing(@PathVariable String username) {
        return ResponseEntity.ok(followService.getFollowing(username));
    }
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularUsers() {
        return ResponseEntity.ok(userService.getPopularUsers());
    }
}