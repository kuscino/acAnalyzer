package it.cusc.acAnalyzer.controller;

import it.cusc.acAnalyzer.model.*;
import it.cusc.acAnalyzer.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalysisController {
    private final AnalysisService analysisService;

    @GetMapping("/session/{sessionId}/lap")
    public ResponseEntity<List<Physics>> getLapData(
            @PathVariable String sessionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant lapStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant lapEnd) {
        return ResponseEntity.ok(analysisService.getLapData(sessionId, lapStart, lapEnd));
    }

    @GetMapping("/session/{sessionId}/highspeed")
    public ResponseEntity<List<Physics>> getHighSpeedMoments(
            @PathVariable String sessionId,
            @RequestParam float minSpeed) {
        return ResponseEntity.ok(analysisService.getHighSpeedMoments(sessionId, minSpeed));
    }

    @GetMapping("/session/{sessionId}/laps")
    public ResponseEntity<List<Graphics>> getCompletedLaps(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(analysisService.getCompletedLaps(sessionId));
    }

    @GetMapping("/session/{sessionId}/state")
    public ResponseEntity<Graphics> getCurrentSessionState(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(analysisService.getCurrentSessionState(sessionId).orElse(null));
    }

    @GetMapping("/sessions/track/{track}")
    public ResponseEntity<List<StaticInfo>> getSessionsByTrack(
            @PathVariable String track) {
        return ResponseEntity.ok(analysisService.findSessionsByTrack(track));
    }

    @GetMapping("/sessions/car/{carModel}")
    public ResponseEntity<List<StaticInfo>> getSessionsByCar(
            @PathVariable String carModel) {
        return ResponseEntity.ok(analysisService.findSessionsByCarModel(carModel));
    }

    @GetMapping("/sessions/player/{playerName}")
    public ResponseEntity<List<StaticInfo>> getSessionsByPlayer(
            @PathVariable String playerName) {
        return ResponseEntity.ok(analysisService.findSessionsByPlayer(playerName));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable String sessionId) {
        analysisService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/session/{sessionId}/stats")
    public ResponseEntity<SessionStats> getSessionStats(
            @PathVariable String sessionId) {
        SessionStats stats = SessionStats.builder()
                .averageSpeed(analysisService.calculateAverageSpeed(sessionId))
                .maxSpeed(analysisService.findMaxSpeed(sessionId))
                .fuelConsumption(analysisService.calculateFuelConsumption(sessionId))
                .tyreWear(analysisService.calculateTyreWear(sessionId))
                .build();
        return ResponseEntity.ok(stats);
    }
}

