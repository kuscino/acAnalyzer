package it.cusc.acAnalyzer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TelemetryStatus {
    private boolean connected;
    private String currentSessionId;
}
