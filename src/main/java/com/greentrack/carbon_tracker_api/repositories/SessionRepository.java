package com.greentrack.carbon_tracker_api.repositories;

import com.greentrack.carbon_tracker_api.entities.Session;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends MongoRepository<Session, String> {
    List<Session> findByUserId(String userId);

    Optional<Session> findByRefreshToken(String refreshToken);

    boolean existsByUserIdAndSessionId(String userId, String sessionId);

    void deleteByUserId(String userId);
}
