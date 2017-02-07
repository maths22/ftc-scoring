package com.maths22.ftc.season.vv;

import com.maths22.ftc.entities.Score;

import javax.persistence.*;

/**
 * Created by jburroughs on 7/6/16.
 */
@Entity
@PrimaryKeyJoinColumn(name="id")
public class SeasonScoreVv extends Score {
    private int autoBeacons = 0;
    private int driverBeacons = 0;
    private boolean autoCapFloor = false;
    private CapPosition driverCapPosition = CapPosition.FLOOR;
    private int autoCenterParticles = 0;
    private int autoCornerParticles = 0;
    private int driverCenterParticles = 0;
    private int driverCornerParticles = 0;
    private RobotPosition r1AutoPos = RobotPosition.FLOOR;
    private RobotPosition r2AutoPos = RobotPosition.FLOOR;

    @Basic
    @Column(name = "auto_beacons", nullable = false)
    public int getAutoBeacons() {
        return autoBeacons;
    }

    public void setAutoBeacons(int autoBeacons) {
        this.autoBeacons = autoBeacons;
    }

    @Basic
    @Column(name = "driver_beacons", nullable = false)
    public int getDriverBeacons() {
        return driverBeacons;
    }

    public void setDriverBeacons(int driverBeacons) {
        this.driverBeacons = driverBeacons;
    }

    @Basic
    @Column(name = "auto_cap_floor", nullable = false)
    public boolean isAutoCapFloor() {
        return autoCapFloor;
    }

    public void setAutoCapFloor(boolean autoCapFloor) {
        this.autoCapFloor = autoCapFloor;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "driver_cap_position", nullable = false)
    public CapPosition getDriverCapPosition() {
        return driverCapPosition;
    }

    public void setDriverCapPosition(CapPosition driverCapPosition) {
        this.driverCapPosition = driverCapPosition;
    }

    @Basic
    @Column(name = "auto_center_particles", nullable = false)
    public int getAutoCenterParticles() {
        return autoCenterParticles;
    }

    public void setAutoCenterParticles(int autoCenterParticles) {
        this.autoCenterParticles = autoCenterParticles;
    }

    @Basic
    @Column(name = "auto_corner_particles", nullable = false)
    public int getAutoCornerParticles() {
        return autoCornerParticles;
    }

    public void setAutoCornerParticles(int autoCornerParticles) {
        this.autoCornerParticles = autoCornerParticles;
    }

    @Basic
    @Column(name = "driver_center_particles", nullable = false)
    public int getDriverCenterParticles() {
        return driverCenterParticles;
    }

    public void setDriverCenterParticles(int driverCenterParticles) {
        this.driverCenterParticles = driverCenterParticles;
    }

    @Basic
    @Column(name = "driver_corner_particles", nullable = false)
    public int getDriverCornerParticles() {
        return driverCornerParticles;
    }

    public void setDriverCornerParticles(int driverCornerParticles) {
        this.driverCornerParticles = driverCornerParticles;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "r1_auto_pos", nullable = false)
    public RobotPosition getR1AutoPos() {
        return r1AutoPos;
    }

    public void setR1AutoPos(RobotPosition r1AutoPos) {
        this.r1AutoPos = r1AutoPos;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "r2_auto_pos", nullable = false)
    public RobotPosition getR2AutoPos() {
        return r2AutoPos;
    }

    public void setR2AutoPos(RobotPosition r2AutoPos) {
        this.r2AutoPos = r2AutoPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SeasonScoreVv that = (SeasonScoreVv) o;

        if (autoBeacons != that.autoBeacons) return false;
        if (driverBeacons != that.driverBeacons) return false;
        if (autoCapFloor != that.autoCapFloor) return false;
        if (autoCenterParticles != that.autoCenterParticles) return false;
        if (autoCornerParticles != that.autoCornerParticles) return false;
        if (driverCenterParticles != that.driverCenterParticles) return false;
        if (driverCornerParticles != that.driverCornerParticles) return false;
        if (driverCapPosition != that.driverCapPosition) return false;
        if (r1AutoPos != that.r1AutoPos) return false;
        return r2AutoPos == that.r2AutoPos;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + autoBeacons;
        result = 31 * result + driverBeacons;
        result = 31 * result + (autoCapFloor ? 1 : 0);
        result = 31 * result + (driverCapPosition != null ? driverCapPosition.hashCode() : 0);
        result = 31 * result + autoCenterParticles;
        result = 31 * result + autoCornerParticles;
        result = 31 * result + driverCenterParticles;
        result = 31 * result + driverCornerParticles;
        result = 31 * result + (r1AutoPos != null ? r1AutoPos.hashCode() : 0);
        result = 31 * result + (r2AutoPos != null ? r2AutoPos.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    public int computeAutoScore() {
        int score = 0;
        score += 30 * this.autoBeacons;
        score += this.autoCapFloor ? 5 : 0;
        score += 15 * this.autoCenterParticles;
        score += 5 * this.autoCornerParticles;
        score += this.r1AutoPos.getAutoScore();
        score += this.r2AutoPos.getAutoScore();
        return score;
    }

    public int computeAutoBonusScore() {
        return 0;
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    public int computeDriverScore() {
        int score = 0;
        score += 10 * this.driverBeacons;
        score += 5 * this.driverCenterParticles;
        score += 1 * this.driverCornerParticles;
        return score;
    }

    public int computeEndgameScore() {
        return this.driverCapPosition.getDriverScore();
    }
}
