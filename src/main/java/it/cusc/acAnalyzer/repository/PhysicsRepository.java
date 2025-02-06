package it.cusc.acAnalyzer.repository;

import it.cusc.acAnalyzer.model.Physics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;


@Repository
public interface PhysicsRepository extends MongoRepository<Physics, String> {
    List<Physics> findBySessionId(String sessionId);
    List<Physics> findBySessionIdAndTimestampBetween(String sessionId, Instant start, Instant end);

    @Query("{'sessionId': ?0, 'speedKmh': { $gt: ?1 }}")
    List<Physics> findHighSpeedMoments(String sessionId, float minSpeed);

    @Query("{'sessionId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 }}")
    List<Physics> findLapData(String sessionId, Instant lapStart, Instant lapEnd);

    void deleteBySessionId(String sessionId);
}

