package it.cusc.acAnalyzer.repository;

import it.cusc.acAnalyzer.model.Graphics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GraphicsRepository extends MongoRepository<Graphics, String> {
    List<Graphics> findBySessionId(String sessionId);
    List<Graphics> findBySessionIdAndTimestampBetween(String sessionId, Instant start, Instant end);

    @Query("{'sessionId': ?0, 'completedLaps': { $gt: 0 }}")
    List<Graphics> findCompletedLaps(String sessionId);

    Optional<Graphics> findFirstBySessionIdOrderByTimestampDesc(String sessionId);

    void deleteBySessionId(String sessionId);
}
