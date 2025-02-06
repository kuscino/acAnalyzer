package it.cusc.acAnalyzer.model;

import it.cusc.acAnalyzer.core.SharedMemoryUtils;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.nio.MappedByteBuffer;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Document(collection = "staticInfo")
public class StaticInfo {
    @Id
    private String id;
    private String sessionId;
    private Instant timestamp;

    private int smVersion;
    private int acVersion;
    private int numberOfSessions;
    private int numCars;
    private String carModel;
    private String track;
    private String playerName;
    private String playerSurname;
    private String playerNick;
    private int sectorCount;
    private float maxTorque;
    private float maxPower;
    private int maxRpm;
    private float maxFuel;
    private float[] suspensionMaxTravel = new float[4];
    private float[] tyreRadius = new float[4];
    private float maxTurboBoost;
    private float deprecated1;
    private float deprecated2;
    private int penaltiesEnabled;
    private float aidFuelRate;
    private float aidTireRate;
    private float aidMechanicalDamage;
    private int aidAllowTyreBlankets;
    private float aidStability;
    private int aidAutoClutch;
    private int aidAutoBlip;
    private int hasDRS;
    private int hasERS;
    private int hasKERS;
    private float kersMaxJ;
    private int engineBrakeSettingsCount;
    private int ersPowerControllerCount;
    private float trackSPlineLength;
    private String trackConfiguration;
    private float ersMaxJ;
    private int isTimedRace;
    private int hasExtraLap;
    private String carSkin;
    private int reversedGridPositions;
    private int pitWindowStart;
    private int pitWindowEnd;
    private int isOnline;
    public static final int BUFFER_SIZE = 1024; // Adjust based on actual struct size

    public void readFromBuffer(MappedByteBuffer buffer) {
        buffer.position(0);

        smVersion = buffer.getInt();
        acVersion = buffer.getInt();
        numberOfSessions = buffer.getInt();
        numCars = buffer.getInt();

        carModel = SharedMemoryUtils.readString(buffer, 33);
        track = SharedMemoryUtils.readString(buffer, 33);
        playerName = SharedMemoryUtils.readString(buffer, 33);
        playerSurname = SharedMemoryUtils.readString(buffer, 33);
        playerNick = SharedMemoryUtils.readString(buffer, 33);

        sectorCount = buffer.getInt();
        maxTorque = buffer.getFloat();
        maxPower = buffer.getFloat();
        maxRpm = buffer.getInt();
        maxFuel = buffer.getFloat();

        SharedMemoryUtils.readFloatArray(buffer, suspensionMaxTravel);
        SharedMemoryUtils.readFloatArray(buffer, tyreRadius);

        maxTurboBoost = buffer.getFloat();
        deprecated1 = buffer.getFloat();
        deprecated2 = buffer.getFloat();

        penaltiesEnabled = buffer.getInt();
        aidFuelRate = buffer.getFloat();
        aidTireRate = buffer.getFloat();
        aidMechanicalDamage = buffer.getFloat();
        aidAllowTyreBlankets = buffer.getInt();
        aidStability = buffer.getFloat();
        aidAutoClutch = buffer.getInt();
        aidAutoBlip = buffer.getInt();

        hasDRS = buffer.getInt();
        hasERS = buffer.getInt();
        hasKERS = buffer.getInt();
        kersMaxJ = buffer.getFloat();
        engineBrakeSettingsCount = buffer.getInt();
        ersPowerControllerCount = buffer.getInt();
        trackSPlineLength = buffer.getFloat();

        trackConfiguration = SharedMemoryUtils.readString(buffer, 33);

        ersMaxJ = buffer.getFloat();
        isTimedRace = buffer.getInt();
        hasExtraLap = buffer.getInt();

        carSkin = SharedMemoryUtils.readString(buffer, 33);

        reversedGridPositions = buffer.getInt();
        pitWindowStart = buffer.getInt();
        pitWindowEnd = buffer.getInt();
        isOnline = buffer.getInt();
    }
}