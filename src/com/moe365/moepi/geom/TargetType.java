package com.moe365.moepi.geom;

/**
 * For Destination Deep Space
 */
public enum TargetType {
    NONE(1), LEFT(2), RIGHT(3);

    private final int targetType;

    private TargetType(int type) {
        this.targetType = type;
    }

    public boolean isLeft() {
        return getType() == 2;
    }

    public boolean isRight() {
        return getType() == 3;
    }

    public int getType() {
        return targetType;
    }

    @Override
    public String toString() {
        switch(getType()) {
            case 1:
                return "NONE";
            case 2:
                return "LEFT";
            case 3:
                return "RIGHT";
            default:
                return "UNKNOWN TYPE";
        }
    }
}
