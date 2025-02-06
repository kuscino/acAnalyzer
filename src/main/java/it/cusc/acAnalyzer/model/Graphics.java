package it.cusc.acAnalyzer.model;

import it.cusc.acAnalyzer.core.SharedMemoryUtils;
import it.cusc.acAnalyzer.model.enums.ACStatus;
import it.cusc.acAnalyzer.model.enums.FlagType;
import it.cusc.acAnalyzer.model.enums.SessionType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.nio.MappedByteBuffer;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Document(collection = "graphics")
public class Graphics {
    @Id
    private String id;
    private String sessionId;
    private Instant timestamp;

    private int packetId;
    private ACStatus status;
    private SessionType session;
    private String currentTime;
    private String lastTime;
    private String bestTime;
    private String split;
    private int completedLaps;
    private int position;
    private int currentTimeMs;
    private int lastTimeMs;
    private int bestTimeMs;
    private float sessionTimeLeft;
    private float distanceTraveled;
    private int isInPit;
    private int currentSectorIndex;
    private int lastSectorTime;
    private int numberOfLaps;
    private String tyreCompound;
    private float replayTimeMultiplier;
    private float normalizedCarPosition;
    private float[] carCoordinates = new float[3];
    private float penaltyTime;
    private FlagType flag;
    private int idealLineOn;
    private int isInPitLane;
    private float surfaceGrip;
    private int mandatoryPitDone;
    public static final int BUFFER_SIZE = 1024; // Adjust based on actual struct size

    public void readFromBuffer(MappedByteBuffer buffer) {
        buffer.position(0);

        packetId = buffer.getInt();
        status = ACStatus.values()[buffer.getInt()];
        session = SessionType.fromValue(buffer.getInt());

        currentTime = SharedMemoryUtils.readString(buffer, 15);
        lastTime = SharedMemoryUtils.readString(buffer, 15);
        bestTime = SharedMemoryUtils.readString(buffer, 15);
        split = SharedMemoryUtils.readString(buffer, 15);

        completedLaps = buffer.getInt();
        position = buffer.getInt();
        currentTimeMs = buffer.getInt();
        lastTimeMs = buffer.getInt();
        bestTimeMs = buffer.getInt();
        sessionTimeLeft = buffer.getFloat();
        distanceTraveled = buffer.getFloat();
        isInPit = buffer.getInt();
        currentSectorIndex = buffer.getInt();
        lastSectorTime = buffer.getInt();
        numberOfLaps = buffer.getInt();

        tyreCompound = SharedMemoryUtils.readString(buffer, 33);

        replayTimeMultiplier = buffer.getFloat();
        normalizedCarPosition = buffer.getFloat();

        SharedMemoryUtils.readFloatArray(buffer, carCoordinates);

        penaltyTime = buffer.getFloat();
        flag = FlagType.values()[buffer.getInt()];
        idealLineOn = buffer.getInt();
        isInPitLane = buffer.getInt();
        surfaceGrip = buffer.getFloat();
        mandatoryPitDone = buffer.getInt();
    }
}

