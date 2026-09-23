package com.seyahood.repository;

import com.seyahood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import com.seyahood.model.Follow;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
    Optional<User> findByRevenueCatCustomerId(String revenueCatCustomerId);

    @Query("SELECT u FROM User u LEFT JOIN Follow f ON f.following = u GROUP BY u ORDER BY COUNT(f) DESC")
    List<User> findPopularUsers(Pageable pageable);
}