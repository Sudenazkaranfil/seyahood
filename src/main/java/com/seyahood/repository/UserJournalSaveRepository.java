package com.seyahood.repository;

import com.seyahood.model.Journal;
import com.seyahood.model.User;
import com.seyahood.model.UserJournalSave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserJournalSaveRepository extends JpaRepository<UserJournalSave, Long> {
    boolean existsByUserAndJournal(User user, Journal journal);
    Optional<UserJournalSave> findByUserAndJournal(User user, Journal journal);
    List<UserJournalSave> findByUser(User user);
    long countByJournal(Journal journal);
}