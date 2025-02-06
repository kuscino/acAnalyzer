package it.cusc.acAnalyzer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionStats {
    private double averageSpeed;
    private float maxSpeed;
    private double fuelConsumption;
    private double tyreWear;
}
