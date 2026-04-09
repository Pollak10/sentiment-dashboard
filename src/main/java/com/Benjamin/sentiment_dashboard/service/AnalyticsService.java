package com.Benjamin.sentiment_dashboard.service;

import com.Benjamin.sentiment_dashboard.model.UserSession;
import com.Benjamin.sentiment_dashboard.model.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private UserSessionRepository repository;

    @Autowired
    private WebSocketService webSocketService;

    public void userJoined(String sessionId, String ipAddress) {
        UserSession session = new UserSession();
        session.setSessionId(sessionId);
        session.setIpAddress(ipAddress);
        session.setJoinedAt(LocalDateTime.now());
        session.setLastSeenAt(LocalDateTime.now());
        session.setSessionDurationSeconds(0L);
        session.setActive(true);
        repository.save(session);
        System.out.println("User joined: " + sessionId);
        broadcastAnalytics();
    }

    public void userLeft(String sessionId) {
        repository.findById(sessionId).ifPresent(session -> {
            session.setActive(false);
            session.setLastSeenAt(LocalDateTime.now());
            long duration = ChronoUnit.SECONDS.between(
                    session.getJoinedAt(),
                    LocalDateTime.now()
            );
            session.setSessionDurationSeconds(duration);
            repository.save(session);
            System.out.println("User left: " + sessionId +
                    " after " + duration + "s");
        });
        broadcastAnalytics();
    }

    public void userHeartbeat(String sessionId) {
        repository.findById(sessionId).ifPresent(session -> {
            session.setLastSeenAt(LocalDateTime.now());
            repository.save(session);
        });
    }

    @Scheduled(fixedRate = 30000)
    public void cleanupInactiveSessions() {
        List<UserSession> activeSessions =
                repository.findByActiveTrue();
        LocalDateTime cutoff =
                LocalDateTime.now().minusSeconds(30);

        for (UserSession session : activeSessions) {
            if (session.getLastSeenAt().isBefore(cutoff)) {
                userLeft(session.getSessionId());
            }
        }
    }

    public Map<String, Object> getAnalytics() {
        long activeUsers = repository.countByActiveTrue();

        List<UserSession> todaySessions =
                repository.findByJoinedAtAfter(
                        LocalDateTime.now().minusHours(24)
                );

        long totalPageViews = todaySessions.size();

        long peakUsers = todaySessions.stream()
                .filter(s -> s.getJoinedAt()
                        .isAfter(LocalDateTime.now().minusHours(24)))
                .count();

        double avgSessionTime = todaySessions.stream()
                .filter(s -> !s.isActive())
                .mapToLong(UserSession::getSessionDurationSeconds)
                .average()
                .orElse(0);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("activeUsers", activeUsers);
        analytics.put("totalPageViews", totalPageViews);
        analytics.put("avgSessionSeconds",
                Math.round(avgSessionTime));
        analytics.put("peakUsers", peakUsers);

        return analytics;
    }

    public void broadcastAnalytics() {
        webSocketService.sendAnalyticsUpdate(getAnalytics());
    }
}