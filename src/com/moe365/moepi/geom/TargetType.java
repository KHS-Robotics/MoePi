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
        return getType() == 0;
    }

    public boolean isRight() {
        return getType() == 1;
    }

    public int getType() {
        return targetType;
    }

    @Override
    public String toString() {
        switch(getType()) {
            case 0:
                return "LEFT";
            case 1:
                return "RIGHT";
            default:
                return "NO TYPE";
        }
    }
}
