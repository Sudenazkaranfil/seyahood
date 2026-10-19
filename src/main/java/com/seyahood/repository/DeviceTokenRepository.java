package com.seyahood.repository;

import com.seyahood.model.DeviceToken;
import com.seyahood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUser(User user);
    Optional<DeviceToken> findByToken(String token);
    void deleteByToken(String token);
}
