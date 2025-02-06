package it.cusc.acAnalyzer.exception;

public class AssettoCorsaNotStartedException extends Exception {
    public AssettoCorsaNotStartedException() {
        super("Shared Memory not connected, is Assetto Corsa running?");
    }

}
