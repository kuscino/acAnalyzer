package it.cusc.acAnalyzer.core;

import it.cusc.acAnalyzer.model.Coordinates;

import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;

public class SharedMemoryUtils {
    public static String readString(MappedByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        // Trova la posizione del primo byte zero (fine stringa C)
        int zeroPos = 0;
        while (zeroPos < bytes.length && bytes[zeroPos] != 0) {
            zeroPos++;
        }
        return new String(bytes, 0, zeroPos, StandardCharsets.UTF_8).trim();
    }

    public static void readFloatArray(MappedByteBuffer buffer, float[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }
    }

    public static void readCoordinatesArray(MappedByteBuffer buffer, Coordinates[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = new Coordinates();
            array[i].setX(buffer.getFloat());
            array[i].setY(buffer.getFloat());
            array[i].setZ(buffer.getFloat());
        }
    }
}
