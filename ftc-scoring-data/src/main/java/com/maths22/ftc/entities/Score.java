package com.maths22.ftc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name = "season_score_type")
public abstract class Score {
    private UUID id;
    private int majorPenalties;
    private int minorPenalties;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", nullable = false)
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Basic
    @Column(name = "major_penalties", nullable = false, length = 45)
    public int getMajorPenalties() {
        return majorPenalties;
    }

    public void setMajorPenalties(int majorPenalties) {
        this.majorPenalties = majorPenalties;
    }

    @Basic
    @Column(name = "minor_penalties", nullable = false, length = 45)
    public int getMinorPenalties() {
        return minorPenalties;
    }

    public void setMinorPenalties(int minorPenalties) {
        this.minorPenalties = minorPenalties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Score score = (Score) o;

        if (majorPenalties != score.majorPenalties) return false;
        return minorPenalties == score.minorPenalties;

    }

    @Override
    public int hashCode() {
        int result = majorPenalties;
        result = 31 * result + minorPenalties;
        return result;
    }

    public int computeFinalScore(Score other) {
        return computeTotalScore()
                + other.computePenaltyScore();
    }

//    @JsonProperty("totalScore")
    public int computeTotalScore() {
        return computeAutoScore() + computeAutoBonusScore() + computeDriverScore() + computeEndgameScore();
    }

    @JsonProperty("autoScore")
    public abstract int computeAutoScore();

    @JsonProperty("autoBonusScore")
    public abstract int computeAutoBonusScore();

    @JsonProperty("driverScore")
    public abstract int computeDriverScore();

    @JsonProperty("endgameScore")
    public abstract int computeEndgameScore();

    @JsonProperty("penaltyScore")
    public int computePenaltyScore() {
        return 40 * this.majorPenalties
             + 10 * this.minorPenalties;
    }
}
