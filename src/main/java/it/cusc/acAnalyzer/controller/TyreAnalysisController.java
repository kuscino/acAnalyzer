package it.cusc.acAnalyzer.controller;

import it.cusc.acAnalyzer.model.TyreSummary;
import it.cusc.acAnalyzer.service.TyreAnalysisService;
import it.cusc.acAnalyzer.service.TyreAnalysisService.TyreAnalysisResult;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis/tyres")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TyreAnalysisController {
    private final TyreAnalysisService tyreAnalysisService;

    @GetMapping("/tracks")
    public ResponseEntity<List<String>> getAvailableTracks() {
        return ResponseEntity.ok(tyreAnalysisService.findAvailableTracks());
    }

    @GetMapping("/tracks/{track}/cars")
    public ResponseEntity<List<String>> getAvailableCarsForTrack(@PathVariable String track) {
        return ResponseEntity.ok(tyreAnalysisService.findAvailableCarsForTrack(track));
    }

    @GetMapping("/track/{track}/car/{carModel}")
    public ResponseEntity<List<TyreAnalysisResult>> analyzeTrackCarCombination(
            @PathVariable String track,
            @PathVariable String carModel) {
        return ResponseEntity.ok(tyreAnalysisService.analyzeTrackCarCombination(track, carModel));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<TyreAnalysisResult> analyzeSession(@PathVariable String sessionId) {
        return ResponseEntity.ok(tyreAnalysisService.analyzeSession(sessionId));
    }

    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<TyreSummary> getSessionSummary(@PathVariable String sessionId) {
        TyreAnalysisResult analysis = tyreAnalysisService.analyzeSession(sessionId);

        TyreSummary summary = TyreSummary.builder()
                .sessionInfo(String.format("%s - %s - %s",
                        analysis.getTrack(),
                        analysis.getCarModel(),
                        analysis.getSessionDate()))
                .totalStints(analysis.getStints().size())
                .averageTemperatures(calculateAverageTemps(analysis))
                .averagePressures(calculateAveragePressures(analysis))
                .totalWear(analysis.getWear().getTotalWear())
                .estimatedTyreLife(analysis.getWear().getEstimatedLife())
                .timeInOptimalTempRange(analysis.getTemperatures().getTimeInOptimalRange())
                .timeInOptimalPressureRange(analysis.getPressures().getTimeInOptimalRange())
                .build();

        return ResponseEntity.ok(summary);
    }

    private double[] calculateAverageTemps(TyreAnalysisResult analysis) {
        double[] avgTemps = new double[4];
        for (int i = 0; i < 4; i++) {
            avgTemps[i] = (analysis.getTemperatures().getAvgInnerTemp()[i] +
                    analysis.getTemperatures().getAvgMiddleTemp()[i] +
                    analysis.getTemperatures().getAvgOuterTemp()[i]) / 3;
        }
        return avgTemps;
    }

    private double[] calculateAveragePressures(TyreAnalysisResult analysis) {
        return analysis.getPressures().getAvgPressure();
    }
}

