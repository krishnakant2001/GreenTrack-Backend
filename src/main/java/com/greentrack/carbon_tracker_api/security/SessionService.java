package com.greentrack.carbon_tracker_api.security;

import com.greentrack.carbon_tracker_api.entities.Session;
import com.greentrack.carbon_tracker_api.repositories.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final int SESSION_LIMIT = 1;

    public void generateNewSession(String userId, String refreshToken, String sessionId) {
        List<Session> userSessions = sessionRepository.findByUserId(userId);

        log.info("Generation of the new session....");

        if (userSessions.size() == SESSION_LIMIT) {
            userSessions.sort(Comparator.comparing(Session::getLastUsedAt));
            Session leastRecentlyUsedSession = userSessions.getFirst();
            sessionRepository.delete(leastRecentlyUsedSession);
        }

        Session newSession = Session.builder()
                .userId(userId)
                .sessionId(sessionId)
                .refreshToken(refreshToken)
                .lastUsedAt(LocalDateTime.now())
                .build();

        sessionRepository.save(newSession);
    }

    public void validateSession(String refreshToken) {
        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new SessionAuthenticationException("Session not found for the refresh token: " + refreshToken));

        session.setLastUsedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    public void deleteSession(String userId) {
        List<Session> sessionList = sessionRepository.findByUserId(userId);

        if(!sessionList.isEmpty()) {
            sessionRepository.deleteByUserId(userId);
            log.info("All sessions deleted for user {}", userId);

            //sessionRepository.deleteAll(sessionList);
        } else {
            log.info("No active sessions found for user {}", userId);
        }
    }
}
