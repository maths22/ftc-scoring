package com.maths22.ftc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maths22.ftc.enums.MatchEventType;
import com.maths22.ftc.enums.MatchResult;
import com.maths22.ftc.enums.MatchType;
import com.maths22.ftc.ranking.Ranking;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by jburroughs on 7/6/16.
 */
@Entity
public class Match implements Comparable<Match> {
    private UUID id;
    private boolean scored = false;
    private MatchType type;
    private int number;
    private Alliance blueAlliance;
    private Alliance redAlliance;
    private Division division;
    private Score redScore;
    private Score blueScore;
    private Collection<MatchEvent> matchEvents = new ArrayList<>();

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
    @Column(name = "scored", nullable = false)
    public boolean isScored() {
        return scored;
    }

    public void setScored(boolean scored) {
        this.scored = scored;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    @Basic
    @Column(name = "number", nullable = false)
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        if (type != match.type) return false;
        if (number != match.number) return false;
        return id != null ? id.equals(match.id) : match.id == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + number;
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "red_alliance", referencedColumnName = "id", nullable = false)
    public Alliance getBlueAlliance() {
        return blueAlliance;
    }

    public void setBlueAlliance(Alliance blueAlliance) {
        this.blueAlliance = blueAlliance;
    }

    @ManyToOne
    @JoinColumn(name = "blue_alliance", referencedColumnName = "id", nullable = false)
    public Alliance getRedAlliance() {
        return redAlliance;
    }

    public void setRedAlliance(Alliance redAlliance) {
        this.redAlliance = redAlliance;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division", referencedColumnName = "id", nullable = false, updatable = false)
    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    @ManyToOne
    @JoinColumn(name = "red_score", referencedColumnName = "id", nullable = false)
    @Cascade(CascadeType.ALL)
    public Score getRedScore() {
        return redScore;
    }

    public void setRedScore(Score redScore) {
        this.redScore = redScore;
    }

    @ManyToOne
    @JoinColumn(name = "blue_score", referencedColumnName = "id", nullable = false)
    @Cascade(CascadeType.ALL)
    public Score getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(Score blueScore) {
        this.blueScore = blueScore;
    }

    @OneToMany(mappedBy = "match", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @org.hibernate.annotations.Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    public Collection<MatchEvent> getMatchEvents() {
        return matchEvents;
    }

    public void setMatchEvents(Collection<MatchEvent> matchEvents) {
        this.matchEvents = matchEvents;
    }

    //Begin non-hibernate methods
    public static int WIN_RP = 2;
    public static int TIE_RP = 1;

    @JsonProperty("winningAlliance")
    public String computeWinningAlliance() {
        switch(computeResult()) {
            case BLUE_WON:
                return "B";
            case RED_WON:
                return "R";
            case TIE:
                return "T";
        }
        return null;
    }

    @JsonProperty("redFinalScore")
    public int computeRedFinalScore() {
        return redScore.computeFinalScore(blueScore);
    }

    @JsonProperty("blueFinalScore")
    public int computeBlueFinalScore() {
        return blueScore.computeFinalScore(redScore);
    }

    public MatchResult computeResult() {
        int redFinalScore = computeRedFinalScore();
        int blueFinalScore = computeBlueFinalScore();
        if(redFinalScore > blueFinalScore) {
            return MatchResult.RED_WON;
        } else if(blueFinalScore > redFinalScore) {
            return MatchResult.BLUE_WON;
        } else {
            return MatchResult.TIE;
        }
    }

    public int computeQp(TeamEventAssignment team) {
        if(!earnsQpRp(team)) {
            return 0;
        }

        boolean isOnRedAlliance = getRedAlliance().getTeams().stream()
                .anyMatch(t -> t.getId().equals(team.getId()));
        boolean isOnBlueAlliance = getBlueAlliance().getTeams().stream()
                .anyMatch(t -> t.getId().equals(team.getId()));
        Assert.isTrue(isOnBlueAlliance || isOnRedAlliance, "Team did not partipate in match");
        MatchResult matchResult = computeResult();
        if (isOnBlueAlliance && matchResult.equals(MatchResult.BLUE_WON)) {
            return WIN_RP;
        } else if (isOnRedAlliance && matchResult.equals(MatchResult.RED_WON)) {
            return WIN_RP;
        } else if(matchResult.equals(MatchResult.TIE)) {
            return TIE_RP;
        } else {
            return 0;
        }
    }

    /* TODO: must match game manual:
    If both Teams on an Alliance are disqualified, the Teams on the winning Alliance are awarded
    their own score as their RP for that Match.
     */

    public int computeRp(TeamEventAssignment team) {
        if(!earnsQpRp(team)) {
            return 0;
        }

        boolean isOnRedAlliance = getRedAlliance().getTeams().stream()
                .anyMatch(t -> t.getId().equals(team.getId()));
        boolean isOnBlueAlliance = getBlueAlliance().getTeams().stream()
                .anyMatch(t -> t.getId().equals(team.getId()));
        Assert.isTrue(isOnBlueAlliance || isOnRedAlliance, "Team did not partipate in match");
        MatchResult matchResult = computeResult();
        if (matchResult.equals(MatchResult.BLUE_WON)) {
            return getRedScore().computeTotalScore();
        } else if (matchResult.equals(MatchResult.RED_WON)) {
            return getBlueScore().computeTotalScore();
        } else {
            return Math.min(getRedScore().computeTotalScore(), getBlueScore().computeTotalScore());
        }
    }

    public boolean earnsQpRp(TeamEventAssignment team) {
        List<MatchEvent> matchEventList = getMatchEvents().stream()
                .filter(evt -> evt.getTeam().getId().equals(team.getId()))
                .collect(Collectors.toList());

        for(MatchEvent evt : matchEventList) {
            if (evt.getType().equals(MatchEventType.DQ)) {
                return false;
            } else if (evt.getType().equals(MatchEventType.RED_CARD)) {
                return false;
            } else if (evt.getType().equals(MatchEventType.NO_SHOW)) {
                return false;
            } else if (evt.getType().equals(MatchEventType.SURROGATE)) {
                return false;
            }
        }
        return true;
    }

    public Ranking computeRanking(TeamEventAssignment team) {
        if(!earnsQpRp(team)) {
            return new Ranking(team.getTeam(), new ArrayList<>(), 0, 0);
        } else {
            List<Integer> scoreList = new ArrayList<>();
            if(this.getRedAlliance().getTeams().contains(team)) {
                scoreList.add(this.computeRedFinalScore()); //m.getRedScore().computeTotalScore();
            } else {
                scoreList.add(this.computeBlueFinalScore()); //.getBlueScore().computeTotalScore();
            }
            return new Ranking(team.getTeam(), scoreList, computeQp(team), computeRp(team));
        }
    }

    public String numberString() {
        StringBuilder ret = new StringBuilder();
        switch (type) {
            case PRACTICE:
                ret.append('P');
                break;
            case QUALIFICATION:
                ret.append('Q');
                break;
            case SEMIFINAL:
                ret.append("SF");
                break;
            case FINAL:
                ret.append('F');
                break;
        }
        ret.append('-');
        if(type.equals(MatchType.SEMIFINAL)) {
            ret.append(number / 10);
            ret.append('-');
            ret.append(number % 10);
        } else {
            ret.append(number);
        }
        return ret.toString();
    }

    @Override
    public int compareTo(Match o) {
        int typeComparision = type.compareTo(o.type);
        if(typeComparision == 0) {
            return Integer.compare(number, o.number);
        } else {
            return typeComparision;
        }
    }
}
