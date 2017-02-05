package com.maths22.ftc.season.vv;

/**
 * Created by jburroughs on 9/12/16.
 */
public enum RobotPosition {
    FLOOR(0),
    PARTIAL_CENTER(5),
    PARTIAL_CORNER(5),
    FULL_CENTER(10),
    FULL_CORNER(10);

    private final int autoScore;

    RobotPosition(int autoScore) {
        this.autoScore = autoScore;
    }

    public int getAutoScore() {
        return autoScore;
    }
}
