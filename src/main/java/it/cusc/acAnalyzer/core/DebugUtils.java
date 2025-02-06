package it.cusc.acAnalyzer.core;

import it.cusc.acAnalyzer.model.Graphics;
import it.cusc.acAnalyzer.model.Physics;
import it.cusc.acAnalyzer.model.StaticInfo;
import it.cusc.acAnalyzer.model.enums.FlagType;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

@Slf4j
public class DebugUtils {

    public static void debugPhysics(Physics physics) {
        log.debug("=== Physics Data Debug ===");
        log.debug("Basic Info:");
        log.debug("  PacketId: {}", physics.getPacketId());
        log.debug("  Speed: {} km/h", physics.getSpeedKmh());
        log.debug("  RPM: {}", physics.getRpms());
        log.debug("  Gear: {}", physics.getGear());

        log.debug("Car Control:");
        log.debug("  Gas: {}", physics.getGas());
        log.debug("  Brake: {}", physics.getBrake());
        log.debug("  Clutch: {}", physics.getClutch());
        log.debug("  SteerAngle: {}", physics.getSteerAngle());

        log.debug("Vehicle Dynamics:");
        log.debug("  Velocity: {}", Arrays.toString(physics.getVelocity()));
        log.debug("  G-Force: {}", Arrays.toString(physics.getAccG()));
        log.debug("  Wheel Slip: {}", Arrays.toString(physics.getWheelSlip()));

        log.debug("Tyre Data:");
        log.debug("  Tyre Wear: {}", Arrays.toString(physics.getTyreWear()));
        log.debug("  Tyre Temp Core: {}", Arrays.toString(physics.getTyreCoreTemperature()));
        log.debug("  Brake Temp: {}", Arrays.toString(physics.getBrakeTemp()));

        log.debug("Systems:");
        log.debug("  DRS: {} (Available: {}, Enabled: {})",
                physics.getDrs(), physics.getDrsAvailable(), physics.getDrsEnabled());
        log.debug("  TC: {}", physics.getTc());
        log.debug("  ABS: {}", physics.getAbs());

        if (physics.getSpeedKmh() > 200) {
            log.warn("High Speed Detected: {} km/h", physics.getSpeedKmh());
        }

        // Controllo usura gomme con ciclo tradizionale invece di stream
        boolean lowTyreWear = false;
        float[] tyreWear = physics.getTyreWear();
        for (float wear : tyreWear) {
            if (wear < 20.0f) {
                lowTyreWear = true;
                break;
            }
        }
        if (lowTyreWear) {
            log.warn("Low Tyre Wear Detected: {}", Arrays.toString(tyreWear));
        }
    }

    public static void debugGraphics(Graphics graphics) {
        log.debug("=== Graphics Data Debug ===");
        log.debug("Session Info:");
        log.debug("  Status: {}", graphics.getStatus());
        log.debug("  Session Type: {}", graphics.getSession());
        log.debug("  CompletedLaps: {}", graphics.getCompletedLaps());
        log.debug("  Position: {}", graphics.getPosition());

        log.debug("Timing:");
        log.debug("  Current Time: {} ({}ms)", graphics.getCurrentTime(), graphics.getCurrentTimeMs());
        log.debug("  Last Time: {} ({}ms)", graphics.getLastTime(), graphics.getLastTimeMs());
        log.debug("  Best Time: {} ({}ms)", graphics.getBestTime(), graphics.getBestTimeMs());
        log.debug("  Session Time Left: {}", graphics.getSessionTimeLeft());

        log.debug("Track Status:");
        log.debug("  Flag: {}", graphics.getFlag());
        log.debug("  Surface Grip: {}", graphics.getSurfaceGrip());
        log.debug("  In Pit: {} (In Pit Lane: {})",
                graphics.getIsInPit() == 1, graphics.getIsInPitLane() == 1);

        if (graphics.getFlag() != FlagType.AC_NO_FLAG) {
            log.warn("Flag Status: {}", graphics.getFlag());
        }
    }

    public static void debugStaticInfo(StaticInfo staticInfo) {
        log.debug("=== Static Info Debug ===");
        log.debug("Version Info:");
        log.debug("  SM Version: {}", staticInfo.getSmVersion());
        log.debug("  AC Version: {}", staticInfo.getAcVersion());

        log.debug("Session Setup:");
        log.debug("  Car: {} (Skin: {})", staticInfo.getCarModel(), staticInfo.getCarSkin());
        log.debug("  Track: {} (Config: {})", staticInfo.getTrack(), staticInfo.getTrackConfiguration());
        log.debug("  Player: {} {} ({})",
                staticInfo.getPlayerName(),
                staticInfo.getPlayerSurname(),
                staticInfo.getPlayerNick());

        log.debug("Car Specs:");
        log.debug("  Max Power: {} hp", staticInfo.getMaxPower());
        log.debug("  Max Torque: {} Nm", staticInfo.getMaxTorque());
        log.debug("  Max RPM: {}", staticInfo.getMaxRpm());
        log.debug("  Max Fuel: {} L", staticInfo.getMaxFuel());

        log.debug("Assists:");
        log.debug("  Fuel Rate: {}", staticInfo.getAidFuelRate());
        log.debug("  Tire Rate: {}", staticInfo.getAidTireRate());
        log.debug("  Mechanical Damage: {}", staticInfo.getAidMechanicalDamage());
        log.debug("  Stability: {}", staticInfo.getAidStability());
        log.debug("  Auto Clutch: {}", staticInfo.getAidAutoClutch() == 1);
        log.debug("  Auto Blip: {}", staticInfo.getAidAutoBlip() == 1);

        log.debug("Systems:");
        log.debug("  DRS: {}", staticInfo.getHasDRS() == 1);
        log.debug("  ERS: {}", staticInfo.getHasERS() == 1);
        log.debug("  KERS: {} (Max: {} J)",
                staticInfo.getHasKERS() == 1,
                staticInfo.getKersMaxJ());
    }

    public static void verifyPhysicsData(Physics physics) {
        // Verifica valori fuori range
        if (physics.getSpeedKmh() < 0 || physics.getSpeedKmh() > 500) {
            log.error("Invalid speed value: {}", physics.getSpeedKmh());
        }

        if (physics.getRpms() < 0 || physics.getRpms() > 20000) {
            log.error("Invalid RPM value: {}", physics.getRpms());
        }

        if (physics.getGear() < -1 || physics.getGear() > 8) {
            log.error("Invalid gear value: {}", physics.getGear());
        }

        // Verifica coerenza dei dati
        if (physics.getSpeedKmh() > 0 && physics.getRpms() == 0) {
            log.warn("Inconsistent data: Speed > 0 but RPM = 0");
        }

        if (physics.getGas() < 0 || physics.getGas() > 1) {
            log.error("Invalid gas value: {}", physics.getGas());
        }

        if (physics.getBrake() < 0 || physics.getBrake() > 1) {
            log.error("Invalid brake value: {}", physics.getBrake());
        }
    }

    public static void verifyGraphicsData(Graphics graphics) {
        // Verifica tempi coerenti
        if (graphics.getBestTimeMs() > 0 &&
                graphics.getCurrentTimeMs() > 0 &&
                graphics.getCurrentTimeMs() < graphics.getBestTimeMs()) {
            log.warn("Current time better than best time: Current={}, Best={}",
                    graphics.getCurrentTimeMs(), graphics.getBestTimeMs());
        }

        if (graphics.getSessionTimeLeft() < 0) {
            log.error("Invalid session time left: {}", graphics.getSessionTimeLeft());
        }

        if (graphics.getCompletedLaps() < 0) {
            log.error("Invalid completed laps: {}", graphics.getCompletedLaps());
        }
    }

    public static void verifyStaticInfo(StaticInfo staticInfo) {
        // Verifica presenza dati essenziali
        if (staticInfo.getCarModel() == null || staticInfo.getCarModel().isEmpty()) {
            log.error("Missing car model information");
        }

        if (staticInfo.getTrack() == null || staticInfo.getTrack().isEmpty()) {
            log.error("Missing track information");
        }

        if (staticInfo.getMaxPower() <= 0) {
            log.error("Invalid max power value: {}", staticInfo.getMaxPower());
        }

        if (staticInfo.getMaxRpm() <= 0) {
            log.error("Invalid max RPM value: {}", staticInfo.getMaxRpm());
        }
    }

    public static void dumpFullState(Physics physics, Graphics graphics, StaticInfo staticInfo) {
        log.info("=====================");
        log.info("Full State Dump");
        log.info("=====================");

        debugPhysics(physics);
        debugGraphics(graphics);
        debugStaticInfo(staticInfo);

        verifyPhysicsData(physics);
        verifyGraphicsData(graphics);
        verifyStaticInfo(staticInfo);

        log.info("=====================");
    }
}
