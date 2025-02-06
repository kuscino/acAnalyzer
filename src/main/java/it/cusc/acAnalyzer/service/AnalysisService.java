package it.cusc.acAnalyzer.service;

import it.cusc.acAnalyzer.model.*;
import it.cusc.acAnalyzer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisService {
    private final PhysicsRepository physicsRepository;
    private final GraphicsRepository graphicsRepository;
    private final StaticInfoRepository staticInfoRepository;

    public List<Physics> getLapData(String sessionId, Instant lapStart, Instant lapEnd) {
        return physicsRepository.findLapData(sessionId, lapStart, lapEnd);
    }

    public List<Physics> getHighSpeedMoments(String sessionId, float minSpeed) {
        return physicsRepository.findHighSpeedMoments(sessionId, minSpeed);
    }

    public List<Graphics> getCompletedLaps(String sessionId) {
        return graphicsRepository.findCompletedLaps(sessionId);
    }

    public Optional<Graphics> getCurrentSessionState(String sessionId) {
        return graphicsRepository.findFirstBySessionIdOrderByTimestampDesc(sessionId);
    }

    public List<StaticInfo> findSessionsByTrack(String track) {
        return staticInfoRepository.findByTrack(track);
    }

    public List<StaticInfo> findSessionsByCarModel(String carModel) {
        return staticInfoRepository.findByCarModel(carModel);
    }

    public List<StaticInfo> findSessionsByPlayer(String playerName) {
        return staticInfoRepository.findByPlayerName(playerName);
    }

    public void deleteSession(String sessionId) {
        physicsRepository.deleteBySessionId(sessionId);
        graphicsRepository.deleteBySessionId(sessionId);
        staticInfoRepository.deleteBySessionId(sessionId);
        log.info("Deleted session: {}", sessionId);
    }

    // Metodi di analisi avanzata

    public double calculateAverageSpeed(String sessionId) {
        List<Physics> physicsData = physicsRepository.findBySessionId(sessionId);
        return physicsData.stream()
                .mapToDouble(Physics::getSpeedKmh)
                .average()
                .orElse(0.0);
    }

    public float findMaxSpeed(String sessionId) {
        List<Physics> physicsData = physicsRepository.findBySessionId(sessionId);
        return (float) physicsData.stream()
                .mapToDouble(Physics::getSpeedKmh)
                .max()
                .orElse(0.0);
    }

    public double calculateFuelConsumption(String sessionId) {
        List<Physics> physicsData = physicsRepository.findBySessionId(sessionId);
        if (physicsData.size() < 2) return 0.0;

        Physics first = physicsData.get(0);
        Physics last = physicsData.get(physicsData.size() - 1);
        return first.getFuel() - last.getFuel();
    }

    public double calculateTyreWear(String sessionId) {
        List<Physics> physicsData = physicsRepository.findBySessionId(sessionId);
        if (physicsData.size() < 2) return 0.0;

        Physics first = physicsData.get(0);
        Physics last = physicsData.get(physicsData.size() - 1);

        // Media dell'usura di tutti e 4 i pneumatici
        double initialWear = 0;
        double finalWear = 0;

        for (int i = 0; i < 4; i++) {
            initialWear += first.getTyreWear()[i];
            finalWear += last.getTyreWear()[i];
        }

        return (initialWear - finalWear) / 4.0;
    }
}
