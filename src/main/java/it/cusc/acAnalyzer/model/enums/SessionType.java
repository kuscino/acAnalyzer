package it.cusc.acAnalyzer.model.enums;

public enum SessionType {
    AC_UNKNOWN(-1),
    AC_PRACTICE(0),
    AC_QUALIFY(1),
    AC_RACE(2),
    AC_HOTLAP(3),
    AC_TIME_ATTACK(4),
    AC_DRIFT(5),
    AC_DRAG(6);

    private final int value;

    SessionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SessionType fromValue(int value) {
        for (SessionType type : SessionType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return AC_UNKNOWN;
    }
}
