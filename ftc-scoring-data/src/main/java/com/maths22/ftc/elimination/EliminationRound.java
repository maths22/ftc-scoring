package com.maths22.ftc.elimination;

import com.maths22.ftc.entities.Alliance;
import com.maths22.ftc.entities.Match;
import com.maths22.ftc.enums.MatchResult;
import com.maths22.ftc.enums.MatchType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Jacob on 2/7/2017.
 */
public abstract class EliminationRound {
    public int MATCHES_TO_WIN = 2;

    private final MatchType matchType;
    private final int roundNumber;

    private List<Match> matchList = new ArrayList<>();

    protected EliminationRound(MatchType matchType, int roundNumber) {
        this.matchType = matchType;
        this.roundNumber = roundNumber;
    }

    public abstract Optional<Alliance> getRedAlliance();
    public abstract Optional<Alliance> getBlueAlliance();

    public abstract Optional<EliminationRound> getRedRound();
    public abstract Optional<EliminationRound> getBlueRound();

    public void setMatchList(List<Match> matchList) {
        this.matchList = Collections.unmodifiableList(matchList);
    }

    //TODO: generify?
    public static EliminationRound fromAlliances(Collection<Alliance> alliances) {
        assert alliances.size() == 4;
        Map<Integer, Alliance> allianceMap = alliances.stream().collect(Collectors.toMap(Alliance::getSeed, Function.identity()));
        EliminationRound highRound = new BaseEliminationRound(MatchType.SEMIFINAL, 1,  allianceMap.get(1), allianceMap.get(4));
        EliminationRound lowRound = new BaseEliminationRound(MatchType.SEMIFINAL, 2, allianceMap.get(2), allianceMap.get(3));
        return new UpperEliminationRound(MatchType.FINAL, 0, highRound, lowRound);
    }

    public String getDisplayName() {
        String ret = "";
        switch(matchType) {
            case SEMIFINAL:
                ret += "SF";
                break;
            case FINAL:
                ret += "F";
                break;
        }

        if(roundNumber > 0) {
            ret += " " + roundNumber;
        }
        return ret;
    }


    public MatchResult getResult() {
        if(!getRedAlliance().isPresent() || !getBlueAlliance().isPresent() || matchList.size() == 0) {
            return MatchResult.TIE;
        }
        int redWins = 0;
        int blueWins = 0;
        for(Match match : matchList) {
            switch(match.computeResult()) {
                case RED_WON:
                    redWins++;
                    break;
                case BLUE_WON:
                    blueWins++;
                    break;
                case TIE:
                    break;
            }
        }
        if(redWins >= MATCHES_TO_WIN) {
            return MatchResult.RED_WON;
        } else if(blueWins >= MATCHES_TO_WIN){
            return MatchResult.BLUE_WON;
        } else {
            return MatchResult.TIE;
        }
    }

    public Optional<Alliance> getWinningAlliance() {
        switch(getResult()) {
            case RED_WON:
                return getRedAlliance();
            case BLUE_WON:
                return getBlueAlliance();
        }
        return Optional.empty();
    }


    private static class BaseEliminationRound extends EliminationRound {
        private Alliance redAlliance;
        private Alliance blueAlliance;

        public BaseEliminationRound(MatchType matchType, int roundNumber, Alliance redAlliance, Alliance blueAlliance) {
            super(matchType, roundNumber);
            this.redAlliance = redAlliance;
            this.blueAlliance = blueAlliance;
        }

        @Override
        public Optional<Alliance> getRedAlliance() {
            return Optional.ofNullable(redAlliance);
        }

        @Override
        public Optional<Alliance> getBlueAlliance() {
            return Optional.ofNullable(blueAlliance);
        }

        @Override
        public Optional<EliminationRound> getRedRound() {
            return Optional.empty();
        }

        @Override
        public Optional<EliminationRound> getBlueRound() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "BaseEliminationRound{" +
                    "redAlliance=" + redAlliance +
                    ", blueAlliance=" + blueAlliance +
                    '}';
        }
    }

    private static class UpperEliminationRound extends EliminationRound {
        private EliminationRound redRound;
        private EliminationRound blueRound;

        public UpperEliminationRound(MatchType matchType, int roundNumber, EliminationRound redRound, EliminationRound blueRound) {
            super(matchType, roundNumber);
            this.redRound = redRound;
            this.blueRound = blueRound;
        }

        @Override
        public Optional<Alliance> getRedAlliance() {
            return redRound.getWinningAlliance();
        }

        @Override
        public Optional<Alliance> getBlueAlliance() {
            return blueRound.getWinningAlliance();
        }

        @Override
        public Optional<EliminationRound> getRedRound() {
            return Optional.of(redRound);
        }

        @Override
        public Optional<EliminationRound> getBlueRound() {
            return Optional.of(blueRound);
        }

        @Override
        public String toString() {
            return "UpperEliminationRound{" +
                    "redRound=" + redRound +
                    ", blueRound=" + blueRound +
                    '}';
        }
    }


}
