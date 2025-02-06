package it.cusc.acAnalyzer.service;

import it.cusc.acAnalyzer.config.TyreProperties;
import it.cusc.acAnalyzer.model.Physics;
import it.cusc.acAnalyzer.model.StaticInfo;
import it.cusc.acAnalyzer.repository.PhysicsRepository;
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
public class TyreAnalysisService {
    private final PhysicsRepository physicsRepository;
    private final StaticInfoRepository staticInfoRepository;
    private final TyreProperties tyreProperties;

    @Data
    @Builder
    public static class TyreAnalysisResult {
        private String sessionId;
        private String track;
        private String carModel;
        private String playerName;
        private String sessionDate;
        private TyreTemperatures temperatures;
        private TyrePressures pressures;
        private TyreWear wear;
        private List<TyreStintData> stints;
    }

    @Data
    @Builder
    public static class TyreTemperatures {
        private double[] avgInnerTemp;    // Media temperatura interna per ogni ruota
        private double[] avgMiddleTemp;    // Media temperatura centrale per ogni ruota
        private double[] avgOuterTemp;     // Media temperatura esterna per ogni ruota
        private double[] maxInnerTemp;     // Massima temperatura interna
        private double[] maxMiddleTemp;    // Massima temperatura centrale
        private double[] maxOuterTemp;     // Massima temperatura esterna
        private double[] optimalRangeMin;  // Range ottimale minimo
        private double[] optimalRangeMax;  // Range ottimale massimo
        private double[] timeInOptimalRange; // Percentuale di tempo nel range ottimale
    }

    @Data
    @Builder
    public static class TyrePressures {
        private double[] avgPressure;      // Pressione media per ogni ruota
        private double[] maxPressure;      // Pressione massima per ogni ruota
        private double[] minPressure;      // Pressione minima per ogni ruota
        private double[] optimalRangeMin;  // Range ottimale minimo
        private double[] optimalRangeMax;  // Range ottimale massimo
        private double[] timeInOptimalRange; // Percentuale di tempo nel range ottimale
    }

    @Data
    @Builder
    public static class TyreWear {
        private double[] totalWear;        // Usura totale per ogni ruota
        private double[] wearRate;         // Tasso di usura per giro
        private double[] estimatedLife;    // Vita stimata delle gomme in giri
    }

    @Data
    @Builder
    public static class TyreStintData {
        private int stintNumber;
        private int startLap;
        private int endLap;
        private int laps;
        private double[] startWear;        // Usura all'inizio dello stint
        private double[] endWear;          // Usura alla fine dello stint
        private double[] avgTemperatures;  // Temperature medie durante lo stint
        private double[] avgPressures;     // Pressioni medie durante lo stint
    }

    public List<String> findAvailableTracks() {
        return staticInfoRepository.findDistinctTracks();
    }

    public List<String> findAvailableCarsForTrack(String track) {
        return staticInfoRepository.findDistinctCarModelsByTrack(track);
    }

    public List<TyreAnalysisResult> analyzeTrackCarCombination(String track, String carModel) {
        List<StaticInfo> sessions = staticInfoRepository.findByTrackAndCarModel(track, carModel);
        return sessions.stream()
                .map(session -> analyzeSession(session.getSessionId()))
                .collect(Collectors.toList());
    }

    public TyreAnalysisResult analyzeSession(String sessionId) {
        StaticInfo sessionInfo = staticInfoRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        List<Physics> physicsData = physicsRepository.findBySessionId(sessionId);

        TyreTemperatures temperatures = analyzeTemperatures(physicsData);
        TyrePressures pressures = analyzePressures(physicsData);
        TyreWear wear = analyzeWear(physicsData);
        List<TyreStintData> stints = analyzeStints(physicsData);

        return TyreAnalysisResult.builder()
                .sessionId(sessionId)
                .track(sessionInfo.getTrack())
                .carModel(sessionInfo.getCarModel())
                .playerName(sessionInfo.getPlayerName())
                .sessionDate(sessionInfo.getTimestamp().toString())
                .temperatures(temperatures)
                .pressures(pressures)
                .wear(wear)
                .stints(stints)
                .build();
    }

    private TyreTemperatures analyzeTemperatures(List<Physics> physicsData) {
        double[] avgInner = new double[4];
        double[] avgMiddle = new double[4];
        double[] avgOuter = new double[4];
        double[] maxInner = new double[4];
        double[] maxMiddle = new double[4];
        double[] maxOuter = new double[4];
        double[] timeInOptimal = new double[4];

        // Ottieni i range dalle properties
        double optimalMin = tyreProperties.getTemp().getOptimal().getMin();
        double optimalMax = tyreProperties.getTemp().getOptimal().getMax();

        for (int i = 0; i < 4; i++) {
            final int wheel = i;
            TyreProperties.TyrePosition position = TyreProperties.TyrePosition.values()[i];
        // Calcola le medie
            avgInner[i] = physicsData.stream()
                    .mapToDouble(p -> p.getTyreTempI()[wheel])
                    .average()
                    .orElse(0.0);

            avgMiddle[i] = physicsData.stream()
                    .mapToDouble(p -> p.getTyreTempM()[wheel])
                    .average()
                    .orElse(0.0);

            avgOuter[i] = physicsData.stream()
                    .mapToDouble(p -> p.getTyreTempO()[wheel])
                    .average()
                    .orElse(0.0);

            // Calcola i massimi
            maxInner[i] = physicsData.stream()
                    .mapToDouble(p -> p.getTyreTempI()[wheel])
                    .max()
                    .orElse(0.0);

            maxMiddle[i] = physicsData.stream()
                    .mapToDouble(p -> p.getTyreTempM()[wheel])
                    .max()
                    .orElse(0.0);

            maxOuter[i] = physicsData.stream()
                    .mapToDouble(p -> p.getTyreTempO()[wheel])
                    .max()
                    .orElse(0.0);

            // Calcola il tempo nel range ottimale (assumiamo 80-100°C come range ottimale)
            double optimalCount = physicsData.stream()
                    .filter(p -> {
                        double avgTemp = (p.getTyreTempI()[wheel] +
                                p.getTyreTempM()[wheel] +
                                p.getTyreTempO()[wheel]) / 3;
                        return tyreProperties.isTemperatureOptimal(avgTemp);
                    })
                    .count();
            timeInOptimal[i] = (optimalCount / physicsData.size()) * 100;        }


        return TyreTemperatures.builder()
                .avgInnerTemp(avgInner)
                .avgMiddleTemp(avgMiddle)
                .avgOuterTemp(avgOuter)
                .maxInnerTemp(maxInner)
                .maxMiddleTemp(maxMiddle)
                .maxOuterTemp(maxOuter)
                .optimalRangeMin(new double[]{optimalMin, optimalMin, optimalMin, optimalMin})
                .optimalRangeMax(new double[]{optimalMax, optimalMax, optimalMax, optimalMax})
                .timeInOptimalRange(timeInOptimal)
                .build();
    }

    private TyrePressures analyzePressures(List<Physics> physicsData) {
        double[] avgPressure = new double[4];
        double[] maxPressure = new double[4];
        double[] minPressure = new double[4];
        double[] timeInOptimal = new double[4];
        double[] optimalRangeMin = new double[4];
        double[] optimalRangeMax = new double[4];

        for (int i = 0; i < 4; i++) {
            final int wheel = i;
            TyreProperties.TyrePosition position = TyreProperties.TyrePosition.values()[i];
            avgPressure[i] = physicsData.stream()
                    .mapToDouble(p -> p.getWheelsPressure()[wheel])
                    .average()
                    .orElse(0.0);

            maxPressure[i] = physicsData.stream()
                    .mapToDouble(p -> p.getWheelsPressure()[wheel])
                    .max()
                    .orElse(0.0);

            minPressure[i] = physicsData.stream()
                    .mapToDouble(p -> p.getWheelsPressure()[wheel])
                    .min()
                    .orElse(0.0);

            // Calcola il tempo nel range ottimale (assumiamo 27-28 PSI come range ottimale)
            TyreProperties.TyrePressure tyrePressure = tyreProperties.getTyrePressureByPosition(position);
            optimalRangeMin[i] = tyrePressure.getOptimal().getMin();
            optimalRangeMax[i] = tyrePressure.getOptimal().getMax();

            // Calcola il tempo nel range ottimale usando i valori configurati
            double optimalCount = physicsData.stream()
                    .filter(p -> tyreProperties.isPressureOptimal(p.getWheelsPressure()[wheel], position))
                    .count();
            timeInOptimal[i] = (optimalCount / physicsData.size()) * 100;
        }

        return TyrePressures.builder()
                .avgPressure(avgPressure)
                .maxPressure(maxPressure)
                .minPressure(minPressure)
                .optimalRangeMin(optimalRangeMin)
                .optimalRangeMax(optimalRangeMax)
                .timeInOptimalRange(timeInOptimal)
                .build();
    }

    private TyreWear analyzeWear(List<Physics> physicsData) {
        if (physicsData.isEmpty()) {
            return null;
        }

        Physics first = physicsData.get(0);
        Physics last = physicsData.get(physicsData.size() - 1);
        int totalLaps = last.getNumberOfTyresOut() - first.getNumberOfTyresOut();

        double[] totalWear = new double[4];
        double[] wearRate = new double[4];
        double[] estimatedLife = new double[4];

        for (int i = 0; i < 4; i++) {
            totalWear[i] = first.getTyreWear()[i] - last.getTyreWear()[i];
            wearRate[i] = totalWear[i] / totalLaps;
            estimatedLife[i] = 100.0 / wearRate[i]; // Assumendo che 100 sia il valore massimo di usura
        }

        return TyreWear.builder()
                .totalWear(totalWear)
                .wearRate(wearRate)
                .estimatedLife(estimatedLife)
                .build();
    }

    private List<TyreStintData> analyzeStints(List<Physics> physicsData) {
        List<TyreStintData> stints = new ArrayList<>();
        int currentStint = 1;
        int startIndex = 0;

        // Identifica i pit stop basandoti sul pitLimiterOn o su cambi significativi nell'usura
        for (int i = 1; i < physicsData.size(); i++) {
            Physics current = physicsData.get(i);
            Physics previous = physicsData.get(i - 1);

            if (current.getPitLimiterOn() == 1 && previous.getPitLimiterOn() == 0) {
                // Fine dello stint
                stints.add(createStintData(currentStint, startIndex, i, physicsData));
                startIndex = i;
                currentStint++;
            }
        }

        // Aggiungi l'ultimo stint
        if (startIndex < physicsData.size() - 1) {
            stints.add(createStintData(currentStint, startIndex, physicsData.size() - 1, physicsData));
        }

        return stints;
    }

    private TyreStintData createStintData(int stintNumber, int startIndex, int endIndex, List<Physics> physicsData) {
        Physics start = physicsData.get(startIndex);
        Physics end = physicsData.get(endIndex);
        List<Physics> stintData = physicsData.subList(startIndex, endIndex + 1);

        double[] avgTemps = new double[4];
        double[] avgPress = new double[4];

        // Converti float[] a double[] per startWear e endWear
        double[] startWearDouble = new double[4];
        double[] endWearDouble = new double[4];

        for (int wheel = 0; wheel < 4; wheel++) {
            final int w = wheel;
            avgTemps[wheel] = stintData.stream()
                    .mapToDouble(p -> (p.getTyreTempI()[w] + p.getTyreTempM()[w] + p.getTyreTempO()[w]) / 3)
                    .average()
                    .orElse(0.0);

            avgPress[wheel] = stintData.stream()
                    .mapToDouble(p -> p.getWheelsPressure()[w])
                    .average()
                    .orElse(0.0);

            // Converti i valori di wear da float a double
            startWearDouble[wheel] = start.getTyreWear()[wheel];
            endWearDouble[wheel] = end.getTyreWear()[wheel];
        }

        return TyreStintData.builder()
                .stintNumber(stintNumber)
                .startLap(start.getNumberOfTyresOut())
                .endLap(end.getNumberOfTyresOut())
                .laps(end.getNumberOfTyresOut() - start.getNumberOfTyresOut())
                .startWear(startWearDouble)  // Ora usiamo l'array di double
                .endWear(endWearDouble)      // Ora usiamo l'array di double
                .avgTemperatures(avgTemps)
                .avgPressures(avgPress)
                .build();
    }
}