package it.cusc.acAnalyzer.controller;

import it.cusc.acAnalyzer.service.LapAnalysisService;
import it.cusc.acAnalyzer.service.LapAnalysisService.LapAnalysisResult;
import it.cusc.acAnalyzer.service.LapAnalysisService.SessionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis/laps")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LapAnalysisController {
    private final LapAnalysisService lapAnalysisService;

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionInfo>> getSessions(
            @RequestParam String track,
            @RequestParam String carModel) {
        return ResponseEntity.ok(lapAnalysisService.findSessions(track, carModel));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<LapAnalysisResult> analyzeSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(lapAnalysisService.analyzeSession(sessionId));
    }
}