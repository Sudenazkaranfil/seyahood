package com.seyahood.service;

import com.seyahood.model.Follow;
import com.seyahood.model.User;
import com.seyahood.repository.FollowRepository;
import com.seyahood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public boolean toggleFollow(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.findByFollowerAndFollowing(follower, following)
                    .ifPresent(followRepository::delete);
            return false;
        } else {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
            return true;
        }
    }

    public boolean isFollowing(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    public long getFollowerCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return followRepository.countByFollowing(user);
    }

    public long getFollowingCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return followRepository.countByFollower(user);
    }
    public List<Map<String, Object>> getFollowers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return followRepository.findByFollowing(user).stream()
                .map(follow -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("username", follow.getFollower().getUsername());
                    map.put("firstName", follow.getFollower().getFirstName());
                    map.put("lastName", follow.getFollower().getLastName());
                    map.put("profileImageUrl", follow.getFollower().getProfileImageUrl());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> getFollowing(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return followRepository.findByFollower(user).stream()
                .map(follow -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("username", follow.getFollowing().getUsername());
                    map.put("firstName", follow.getFollowing().getFirstName());
                    map.put("lastName", follow.getFollowing().getLastName());
                    map.put("profileImageUrl", follow.getFollowing().getProfileImageUrl());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}