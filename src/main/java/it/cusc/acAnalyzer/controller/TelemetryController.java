package it.cusc.acAnalyzer.controller;

import it.cusc.acAnalyzer.model.TelemetryStatus;
import it.cusc.acAnalyzer.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TelemetryController {
    private final TelemetryService telemetryService;

    @PostMapping("/start")
    public ResponseEntity<String> startTelemetry() {
        telemetryService.startTelemetry();
        return ResponseEntity.ok("Telemetry session started with ID: " + telemetryService.getCurrentSessionId());
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopTelemetry() {
        telemetryService.stopTelemetry();
        return ResponseEntity.ok("Telemetry session stopped");
    }

    @GetMapping("/status")
    public ResponseEntity<TelemetryStatus> getTelemetryStatus() {
        TelemetryStatus status = TelemetryStatus.builder()
                .connected(telemetryService.isConnected())
                .currentSessionId(telemetryService.getCurrentSessionId())
                .build();
        return ResponseEntity.ok(status);
    }
}


