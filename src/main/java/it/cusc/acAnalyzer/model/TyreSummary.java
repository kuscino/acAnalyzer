package it.cusc.acAnalyzer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TyreSummary {
    private String sessionInfo;
    private int totalStints;
    private double[] averageTemperatures;
    private double[] averagePressures;
    private double[] totalWear;
    private double[] estimatedTyreLife;
    private double[] timeInOptimalTempRange;
    private double[] timeInOptimalPressureRange;
}
