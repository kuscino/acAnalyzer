package it.cusc.acAnalyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tyre")
@Data
public class TyreProperties {
    private Temperature temp = new Temperature();
    private Pressure pressure = new Pressure();

    @Data
    public static class Temperature {
        private Range optimal = new Range();
        private Range warning = new Range();
        private Range critical = new Range();
    }

    @Data
    public static class Pressure {
        private TyrePressure fl = new TyrePressure();
        private TyrePressure fr = new TyrePressure();
        private TyrePressure rl = new TyrePressure();
        private TyrePressure rr = new TyrePressure();
    }

    @Data
    public static class TyrePressure {
        private Range optimal = new Range();
        private Range warning = new Range();
    }

    @Data
    public static class Range {
        private double min;
        private double max;
    }

    // Metodi di utilità
    public boolean isTemperatureOptimal(double temp) {
        return isInRange(temp, this.temp.getOptimal());
    }

    public boolean isTemperatureWarning(double temp) {
        return isInRange(temp, this.temp.getWarning()) && !isTemperatureOptimal(temp);
    }

    public boolean isTemperatureCritical(double temp) {
        return isInRange(temp, this.temp.getCritical()) && !isTemperatureWarning(temp);
    }

    public boolean isPressureOptimal(double pressure, TyrePosition position) {
        TyrePressure tyrePressure = getTyrePressureByPosition(position);
        return isInRange(pressure, tyrePressure.getOptimal());
    }

    public boolean isPressureWarning(double pressure, TyrePosition position) {
        TyrePressure tyrePressure = getTyrePressureByPosition(position);
        return isInRange(pressure, tyrePressure.getWarning()) && !isPressureOptimal(pressure, position);
    }

    private boolean isInRange(double value, Range range) {
        return value >= range.getMin() && value <= range.getMax();
    }

    public TyrePressure getTyrePressureByPosition(TyrePosition position) {
        return switch (position) {
            case FRONT_LEFT -> pressure.getFl();
            case FRONT_RIGHT -> pressure.getFr();
            case REAR_LEFT -> pressure.getRl();
            case REAR_RIGHT -> pressure.getRr();
        };
    }

    public enum TyrePosition {
        FRONT_LEFT,
        FRONT_RIGHT,
        REAR_LEFT,
        REAR_RIGHT
    }
}
