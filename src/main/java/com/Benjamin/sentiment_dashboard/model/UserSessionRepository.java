package com.Benjamin.sentiment_dashboard.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserSessionRepository
        extends JpaRepository<UserSession, String> {

    List<UserSession> findByActiveTrue();

    long countByActiveTrue();

    List<UserSession> findByJoinedAtAfter(
            java.time.LocalDateTime since
    );
}