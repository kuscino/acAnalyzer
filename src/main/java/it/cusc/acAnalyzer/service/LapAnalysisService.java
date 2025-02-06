package it.cusc.acAnalyzer.service;

import it.cusc.acAnalyzer.model.Graphics;
import it.cusc.acAnalyzer.model.StaticInfo;
import it.cusc.acAnalyzer.repository.GraphicsRepository;
import it.cusc.acAnalyzer.repository.StaticInfoRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LapAnalysisService {
    private final GraphicsRepository graphicsRepository;
    private final StaticInfoRepository staticInfoRepository;

    @Data
    @Builder
    public static class LapAnalysisResult {
        private String sessionId;
        private String track;
        private String carModel;
        private String playerName;
        private List<LapData> laps;
        private BestTimes bestTimes;
        private double consistency;  // percentuale di consistenza nei tempi
        private String sessionDate;  // data della sessione
    }

    @Data
    @Builder
    public static class LapData {
        private int lapNumber;
        private String lapTime;      // formato "1:23.456"
        private int lapTimeMs;       // tempo in millisecondi
        private String[] sectors;    // tempi dei settori ["33.100", "25.200", "25.156"]
        private int[] sectorTimesMs; // tempi dei settori in millisecondi
        private boolean valid;
    }

    @Data
    @Builder
    public static class BestTimes {
        private String bestLapTime;
        private int bestLapTimeMs;
        private String[] bestSectors;    // migliori tempi per ogni settore
        private int[] bestSectorsMs;
        private String theoreticalBest;  // somma dei migliori settori
        private int theoreticalBestMs;
        private String potentialGain;    // differenza tra miglior giro e teorico
    }

    /**
     * Trova tutte le sessioni disponibili per un dato tracciato e auto
     */
    public List<SessionInfo> findSessions(String track, String carModel) {
        List<StaticInfo> sessions = staticInfoRepository.findByTrackAndCarModel(track, carModel);

        return sessions.stream()
                .map(session -> SessionInfo.builder()
                        .sessionId(session.getSessionId())
                        .track(session.getTrack())
                        .carModel(session.getCarModel())
                        .playerName(session.getPlayerName())
                        .date(session.getTimestamp().toString())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Analizza una specifica sessione
     */
    public LapAnalysisResult analyzeSession(String sessionId) {
        StaticInfo sessionInfo = staticInfoRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Graphics> laps = graphicsRepository.findBySessionId(sessionId);

        // Organizziamo i giri
        List<LapData> lapDataList = analyzeLaps(laps);

        // Calcoliamo i best times
        BestTimes bestTimes = calculateBestTimes(lapDataList);

        // Calcoliamo la consistenza
        double consistency = calculateConsistency(lapDataList);

        return LapAnalysisResult.builder()
                .sessionId(sessionId)
                .track(sessionInfo.getTrack())
                .carModel(sessionInfo.getCarModel())
                .playerName(sessionInfo.getPlayerName())
                .laps(lapDataList)
                .bestTimes(bestTimes)
                .consistency(consistency)
                .sessionDate(sessionInfo.getTimestamp().toString())
                .build();
    }

    private List<LapData> analyzeLaps(List<Graphics> laps) {
        List<LapData> lapDataList = new ArrayList<>();

        for (int i = 0; i < laps.size() - 1; i++) {
            Graphics current = laps.get(i);
            Graphics next = laps.get(i + 1);

            if (next.getCompletedLaps() > current.getCompletedLaps()) {
                String[] sectors = new String[3];
                int[] sectorTimesMs = new int[3];

                // Recupera i tempi dei settori
                for (int s = 0; s < 3; s++) {
                    if (s == current.getCurrentSectorIndex() - 1) {
                        sectorTimesMs[s] = current.getLastSectorTime();
                        sectors[s] = formatTime(sectorTimesMs[s]);
                    }
                }

                LapData lapData = LapData.builder()
                        .lapNumber(next.getCompletedLaps())
                        .lapTime(next.getLastTime())
                        .lapTimeMs(next.getLastTimeMs())
                        .sectors(sectors)
                        .sectorTimesMs(sectorTimesMs)
                        .valid(isLapValid(next))
                        .build();

                lapDataList.add(lapData);
            }
        }

        return lapDataList;
    }

    private BestTimes calculateBestTimes(List<LapData> laps) {
        // Troviamo il miglior giro valido
        LapData bestLap = laps.stream()
                .filter(LapData::isValid)
                .min(Comparator.comparingInt(LapData::getLapTimeMs))
                .orElse(null);

        if (bestLap == null) {
            return null;
        }

        // Troviamo i migliori settori
        int[] bestSectorsMs = new int[3];
        String[] bestSectors = new String[3];

        for (int sector = 0; sector < 3; sector++) {
            final int s = sector;
            bestSectorsMs[s] = laps.stream()
                    .filter(LapData::isValid)
                    .mapToInt(lap -> lap.getSectorTimesMs()[s])
                    .min()
                    .orElse(0);
            bestSectors[s] = formatTime(bestSectorsMs[s]);
        }

        // Calcoliamo il tempo teorico
        int theoreticalBestMs = Arrays.stream(bestSectorsMs).sum();
        String theoreticalBest = formatTime(theoreticalBestMs);

        // Calcoliamo il potenziale miglioramento
        String potentialGain = formatTime(bestLap.getLapTimeMs() - theoreticalBestMs);

        return BestTimes.builder()
                .bestLapTime(bestLap.getLapTime())
                .bestLapTimeMs(bestLap.getLapTimeMs())
                .bestSectors(bestSectors)
                .bestSectorsMs(bestSectorsMs)
                .theoreticalBest(theoreticalBest)
                .theoreticalBestMs(theoreticalBestMs)
                .potentialGain(potentialGain)
                .build();
    }

    private double calculateConsistency(List<LapData> laps) {
        List<Integer> validTimes = laps.stream()
                .filter(LapData::isValid)
                .map(LapData::getLapTimeMs)
                .collect(Collectors.toList());

        if (validTimes.isEmpty()) {
            return 0.0;
        }

        double mean = validTimes.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double variance = validTimes.stream()
                .mapToDouble(time -> {
                    double diff = time - mean;
                    return diff * diff;
                })
                .average()
                .orElse(0.0);

        // Ritorna percentuale di consistenza (100% = perfettamente consistente)
        return 100.0 * (1.0 - Math.sqrt(variance) / mean);
    }

    private boolean isLapValid(Graphics lap) {
        return lap.getLastTimeMs() > 0 &&
                lap.getLastTime() != null &&
                !lap.getLastTime().isEmpty();
    }

    private String formatTime(int timeMs) {
        if (timeMs <= 0) return "--:--:---";
        int minutes = timeMs / (60 * 1000);
        int seconds = (timeMs % (60 * 1000)) / 1000;
        int millis = timeMs % 1000;
        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }

    @Data
    @Builder
    public static class SessionInfo {
        private String sessionId;
        private String track;
        private String carModel;
        private String playerName;
        private String date;
    }
}
