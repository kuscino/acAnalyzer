package it.cusc.acAnalyzer.service;

import it.cusc.acAnalyzer.core.AssettoCorsa;
import it.cusc.acAnalyzer.core.GraphicsListener;
import it.cusc.acAnalyzer.core.PhysicsListener;
import it.cusc.acAnalyzer.core.StaticInfoListener;
import it.cusc.acAnalyzer.model.*;
import it.cusc.acAnalyzer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelemetryService implements PhysicsListener, GraphicsListener, StaticInfoListener {
    private final PhysicsRepository physicsRepository;
    private final GraphicsRepository graphicsRepository;
    private final StaticInfoRepository staticInfoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private AssettoCorsa assettoCorsa;
    private String currentSessionId;
    private StaticInfo currentStaticInfo;
    private boolean isConnected = false;

    public void startTelemetry() {
        if (!isConnected) {
            currentSessionId = UUID.randomUUID().toString();
            assettoCorsa = new AssettoCorsa();

            // Registra i listener
            assettoCorsa.addPhysicsListener(this);
            assettoCorsa.addGraphicsListener(this);
            assettoCorsa.addStaticInfoListener(this);

            // Avvia la connessione
            assettoCorsa.start();
            isConnected = true;

            log.info("Started AC telemetry session: {}", currentSessionId);
        }
    }

    public void stopTelemetry() {
        if (isConnected && assettoCorsa != null) {
            assettoCorsa.stop();
            isConnected = false;
            log.info("Stopped AC telemetry session: {}", currentSessionId);
        }
    }

    @Override
    public void onPhysicsUpdate(Physics physics) {
        try {
            // Aggiungi metadati
            physics.setSessionId(currentSessionId);
            physics.setTimestamp(Instant.now());

            // Salva su MongoDB
            physicsRepository.save(physics);

            // Invia update via WebSocket
            messagingTemplate.convertAndSend("/topic/physics", physics);

        } catch (Exception e) {
            log.error("Error processing physics update", e);
        }
    }

    @Override
    public void onGraphicsUpdate(Graphics graphics) {
        try {
            // Aggiungi metadati
            graphics.setSessionId(currentSessionId);
            graphics.setTimestamp(Instant.now());

            // Salva su MongoDB
            graphicsRepository.save(graphics);

            // Invia update via WebSocket
            messagingTemplate.convertAndSend("/topic/graphics", graphics);

        } catch (Exception e) {
            log.error("Error processing graphics update", e);
        }
    }

    @Override
    public void onStaticInfoUpdate(StaticInfo staticInfo) {
        try {
            // Aggiungi metadati
            staticInfo.setSessionId(currentSessionId);
            staticInfo.setTimestamp(Instant.now());

            // Aggiorna il riferimento corrente
            this.currentStaticInfo = staticInfo;

            // Salva su MongoDB
            staticInfoRepository.save(staticInfo);

            // Invia update via WebSocket
            messagingTemplate.convertAndSend("/topic/staticInfo", staticInfo);

        } catch (Exception e) {
            log.error("Error processing static info update", e);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getCurrentSessionId() {
        return currentSessionId;
    }
}