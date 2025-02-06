package it.cusc.acAnalyzer.core;

import it.cusc.acAnalyzer.exception.AssettoCorsaNotStartedException;
import it.cusc.acAnalyzer.model.Graphics;
import it.cusc.acAnalyzer.model.Physics;
import it.cusc.acAnalyzer.model.StaticInfo;
import it.cusc.acAnalyzer.model.enums.ACStatus;
import it.cusc.acAnalyzer.model.enums.MemoryStatus;
import lombok.extern.slf4j.Slf4j;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class AssettoCorsa {
    private Timer sharedMemoryRetryTimer;
    private MemoryStatus memoryStatus = MemoryStatus.DISCONNECTED;
    private ACStatus gameStatus = ACStatus.AC_OFF;

    private MappedByteBuffer physicsBuffer;
    private MappedByteBuffer graphicsBuffer;
    private MappedByteBuffer staticInfoBuffer;

    private Timer physicsTimer;
    private Timer graphicsTimer;
    private Timer staticInfoTimer;

    private final List<PhysicsListener> physicsListeners = new ArrayList<>();
    private final List<GraphicsListener> graphicsListeners = new ArrayList<>();
    private final List<StaticInfoListener> staticInfoListeners = new ArrayList<>();

    private static final String PHYSICS_MAP_NAME = "Local\\acpmf_physics";
    private static final String GRAPHICS_MAP_NAME = "Local\\acpmf_graphics";
    private static final String STATIC_MAP_NAME = "Local\\acpmf_static";

    public AssettoCorsa() {
        sharedMemoryRetryTimer = new Timer("AC-Retry");
        physicsTimer = new Timer("AC-Physics");
        graphicsTimer = new Timer("AC-Graphics");
        staticInfoTimer = new Timer("AC-Static");
        stop();
    }

    public void start() {
        sharedMemoryRetryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                connectToSharedMemory();
            }
        }, 0, 2000);
    }

    private boolean connectToSharedMemory() {
        try {
            memoryStatus = MemoryStatus.CONNECTING;

            // Windows shared memory files
            RandomAccessFile physicsFile = new RandomAccessFile("\\\\.\\Global\\" + PHYSICS_MAP_NAME, "rw");
            physicsBuffer = physicsFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, Physics.BUFFER_SIZE);

            RandomAccessFile graphicsFile = new RandomAccessFile("\\\\.\\Global\\" + GRAPHICS_MAP_NAME, "rw");
            graphicsBuffer = graphicsFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, Graphics.BUFFER_SIZE);

            RandomAccessFile staticFile = new RandomAccessFile("\\\\.\\Global\\" + STATIC_MAP_NAME, "rw");
            staticInfoBuffer = staticFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, StaticInfo.BUFFER_SIZE);

            startTimers();

            sharedMemoryRetryTimer.cancel();
            memoryStatus = MemoryStatus.CONNECTED;
            return true;

        } catch (Exception e) {
            log.error("Failed to connect to AC shared memory", e);
            stopTimers();
            return false;
        }
    }

    private void startTimers() {
        // Physics updates (10ms)
        physicsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processPhysics();
            }
        }, 0, 10);

        // Graphics updates (1000ms)
        graphicsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processGraphics();
            }
        }, 0, 1000);

        // Static info updates (1000ms)
        staticInfoTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                processStaticInfo();
            }
        }, 0, 1000);
    }

    private void stopTimers() {
        physicsTimer.cancel();
        graphicsTimer.cancel();
        staticInfoTimer.cancel();

        // Ricrea i timer
        physicsTimer = new Timer("AC-Physics");
        graphicsTimer = new Timer("AC-Graphics");
        staticInfoTimer = new Timer("AC-Static");
    }

    public void stop() {
        memoryStatus = MemoryStatus.DISCONNECTED;
        if (sharedMemoryRetryTimer != null) {
            sharedMemoryRetryTimer.cancel();
            sharedMemoryRetryTimer = new Timer("AC-Retry");
        }
        stopTimers();

        physicsBuffer = null;
        graphicsBuffer = null;
        staticInfoBuffer = null;
    }

    private void processPhysics() {
        if (memoryStatus == MemoryStatus.DISCONNECTED) return;

        try {
            Physics physics = readPhysics();
            notifyPhysicsListeners(physics);
        } catch (AssettoCorsaNotStartedException e) {
            log.warn("AC not running during physics update");
        } catch (Exception e) {
            log.error("Error processing physics update", e);
        }
    }

    private void processGraphics() {
        if (memoryStatus == MemoryStatus.DISCONNECTED) return;

        try {
            Graphics graphics = readGraphics();
            notifyGraphicsListeners(graphics);

            if (gameStatus != graphics.getStatus()) {
                gameStatus = graphics.getStatus();
                // Qui potresti aggiungere un notificatore per il cambio di stato del gioco
            }
        } catch (AssettoCorsaNotStartedException e) {
            log.warn("AC not running during graphics update");
        } catch (Exception e) {
            log.error("Error processing graphics update", e);
        }
    }

    private void processStaticInfo() {
        if (memoryStatus == MemoryStatus.DISCONNECTED) return;

        try {
            StaticInfo staticInfo = readStaticInfo();
            notifyStaticInfoListeners(staticInfo);
        } catch (AssettoCorsaNotStartedException e) {
            log.warn("AC not running during static info update");
        } catch (Exception e) {
            log.error("Error processing static info update", e);
        }
    }

    public Physics readPhysics() throws AssettoCorsaNotStartedException {
        if (memoryStatus == MemoryStatus.DISCONNECTED || physicsBuffer == null) {
            throw new AssettoCorsaNotStartedException();
        }

        Physics physics = new Physics();
        physics.readFromBuffer(physicsBuffer);
        return physics;
    }

    public Graphics readGraphics() throws AssettoCorsaNotStartedException {
        if (memoryStatus == MemoryStatus.DISCONNECTED || graphicsBuffer == null) {
            throw new AssettoCorsaNotStartedException();
        }

        Graphics graphics = new Graphics();
        graphics.readFromBuffer(graphicsBuffer);
        return graphics;
    }

    public StaticInfo readStaticInfo() throws AssettoCorsaNotStartedException {
        if (memoryStatus == MemoryStatus.DISCONNECTED || staticInfoBuffer == null) {
            throw new AssettoCorsaNotStartedException();
        }

        StaticInfo staticInfo = new StaticInfo();
        staticInfo.readFromBuffer(staticInfoBuffer);
        return staticInfo;
    }

    // Gestione Listeners
    public void addPhysicsListener(PhysicsListener listener) {
        physicsListeners.add(listener);
    }

    public void addGraphicsListener(GraphicsListener listener) {
        graphicsListeners.add(listener);
    }

    public void addStaticInfoListener(StaticInfoListener listener) {
        staticInfoListeners.add(listener);
    }

    private void notifyPhysicsListeners(Physics physics) {
        for (PhysicsListener listener : physicsListeners) {
            listener.onPhysicsUpdate(physics);
        }
    }

    private void notifyGraphicsListeners(Graphics graphics) {
        for (GraphicsListener listener : graphicsListeners) {
            listener.onGraphicsUpdate(graphics);
        }
    }

    private void notifyStaticInfoListeners(StaticInfo staticInfo) {
        for (StaticInfoListener listener : staticInfoListeners) {
            listener.onStaticInfoUpdate(staticInfo);
        }
    }

    public boolean isRunning() {
        return memoryStatus == MemoryStatus.CONNECTED;
    }

}

