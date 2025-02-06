package it.cusc.acAnalyzer.repository;

import it.cusc.acAnalyzer.model.StaticInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaticInfoRepository extends MongoRepository<StaticInfo, String> {
    Optional<StaticInfo> findBySessionId(String sessionId);
    List<StaticInfo> findByCarModel(String carModel);
    List<StaticInfo> findByTrack(String track);
    List<StaticInfo> findByPlayerName(String playerName);
    List<StaticInfo> findByTrackAndCarModel(String track, String carModel);

    @Query("{'timestamp': { $gte: ?0, $lte: ?1 }}")
    List<StaticInfo> findSessionsInTimeRange(Instant start, Instant end);
    @Query(value = "{}", fields = "{ 'track' : 1 }")
    List<String> findDistinctTracks();

    @Query(value = "{ 'track' : ?0 }", fields = "{ 'carModel' : 1 }")
    List<String> findDistinctCarModelsByTrack(String track);

    void deleteBySessionId(String sessionId);
}
