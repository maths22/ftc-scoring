package com.maths22.ftc.season.vv;

/**
 * Created by jburroughs on 9/12/16.
 */
public enum CapPosition {
    FLOOR(0),
    LOW(10),
    HIGH(20),
    VORTEX(40);

    private final int driverScore;

    CapPosition(int driverScore) {
        this.driverScore = driverScore;
    }

    public int getDriverScore() {
        return driverScore;
    }
}
