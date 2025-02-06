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
@Document(collection = "physics")
public class Physics {
    @Id
    private String id;
    private String sessionId;
    private Instant timestamp;

    private int packetId;
    private float gas;
    private float brake;
    private float fuel;
    private int gear;
    private int rpms;
    private float steerAngle;
    private float speedKmh;
    private float[] velocity = new float[3];
    private float[] accG = new float[3];
    private float[] wheelSlip = new float[4];
    private float[] wheelLoad = new float[4];
    private float[] wheelsPressure = new float[4];
    private float[] wheelAngularSpeed = new float[4];
    private float[] tyreWear = new float[4];
    private float[] tyreDirtyLevel = new float[4];
    private float[] tyreCoreTemperature = new float[4];
    private float[] camberRad = new float[4];
    private float[] suspensionTravel = new float[4];
    private float drs;
    private float tc;
    private float heading;
    private float pitch;
    private float roll;
    private float cgHeight;
    private float[] carDamage = new float[5];
    private int numberOfTyresOut;
    private int pitLimiterOn;
    private float abs;
    private float kersCharge;
    private float kersInput;
    private int autoShifterOn;
    private float[] rideHeight = new float[2];
    private float turboBoost;
    private float ballast;
    private float airDensity;
    private float airTemp;
    private float roadTemp;
    private float[] localAngularVelocity = new float[3];
    private float finalFF;
    private float performanceMeter;
    private int engineBrake;
    private int ersRecoveryLevel;
    private int ersPowerLevel;
    private int ersHeatCharging;
    private int ersIsCharging;
    private float kersCurrentKJ;
    private int drsAvailable;
    private int drsEnabled;
    private float[] brakeTemp = new float[4];
    private float clutch;
    private float[] tyreTempI = new float[4];
    private float[] tyreTempM = new float[4];
    private float[] tyreTempO = new float[4];
    private int isAIControlled;
    private Coordinates[] tyreContactPoint = new Coordinates[4];
    private Coordinates[] tyreContactNormal = new Coordinates[4];
    private Coordinates[] tyreContactHeading = new Coordinates[4];
    private float brakeBias;
    private float[] localVelocity = new float[3];
    public static final int BUFFER_SIZE = 1024; // Adjust based on actual struct size

    public void readFromBuffer(MappedByteBuffer buffer) {
        buffer.position(0);

        packetId = buffer.getInt();
        gas = buffer.getFloat();
        brake = buffer.getFloat();
        fuel = buffer.getFloat();
        gear = buffer.getInt();
        rpms = buffer.getInt();
        steerAngle = buffer.getFloat();
        speedKmh = buffer.getFloat();

        SharedMemoryUtils.readFloatArray(buffer, velocity);
        SharedMemoryUtils.readFloatArray(buffer, accG);
        SharedMemoryUtils.readFloatArray(buffer, wheelSlip);
        SharedMemoryUtils.readFloatArray(buffer, wheelLoad);
        SharedMemoryUtils.readFloatArray(buffer, wheelsPressure);
        SharedMemoryUtils.readFloatArray(buffer, wheelAngularSpeed);
        SharedMemoryUtils.readFloatArray(buffer, tyreWear);
        SharedMemoryUtils.readFloatArray(buffer, tyreDirtyLevel);
        SharedMemoryUtils.readFloatArray(buffer, tyreCoreTemperature);
        SharedMemoryUtils.readFloatArray(buffer, camberRad);
        SharedMemoryUtils.readFloatArray(buffer, suspensionTravel);

        drs = buffer.getFloat();
        tc = buffer.getFloat();
        heading = buffer.getFloat();
        pitch = buffer.getFloat();
        roll = buffer.getFloat();
        cgHeight = buffer.getFloat();

        SharedMemoryUtils.readFloatArray(buffer, carDamage);

        numberOfTyresOut = buffer.getInt();
        pitLimiterOn = buffer.getInt();
        abs = buffer.getFloat();

        kersCharge = buffer.getFloat();
        kersInput = buffer.getFloat();
        autoShifterOn = buffer.getInt();

        SharedMemoryUtils.readFloatArray(buffer, rideHeight);

        turboBoost = buffer.getFloat();
        ballast = buffer.getFloat();
        airDensity = buffer.getFloat();
        airTemp = buffer.getFloat();
        roadTemp = buffer.getFloat();

        SharedMemoryUtils.readFloatArray(buffer, localAngularVelocity);
        finalFF = buffer.getFloat();
        performanceMeter = buffer.getFloat();

        engineBrake = buffer.getInt();
        ersRecoveryLevel = buffer.getInt();
        ersPowerLevel = buffer.getInt();
        ersHeatCharging = buffer.getInt();
        ersIsCharging = buffer.getInt();
        kersCurrentKJ = buffer.getFloat();
        drsAvailable = buffer.getInt();
        drsEnabled = buffer.getInt();

        SharedMemoryUtils.readFloatArray(buffer, brakeTemp);
        clutch = buffer.getFloat();

        SharedMemoryUtils.readFloatArray(buffer, tyreTempI);
        SharedMemoryUtils.readFloatArray(buffer, tyreTempM);
        SharedMemoryUtils.readFloatArray(buffer, tyreTempO);

        isAIControlled = buffer.getInt();

        SharedMemoryUtils.readCoordinatesArray(buffer, tyreContactPoint);
        SharedMemoryUtils.readCoordinatesArray(buffer, tyreContactNormal);
        SharedMemoryUtils.readCoordinatesArray(buffer, tyreContactHeading);

        brakeBias = buffer.getFloat();

        SharedMemoryUtils.readFloatArray(buffer, localVelocity);
    }

}


