package com.maths22.ftc.ranking;

import com.maths22.ftc.entities.*;
import com.maths22.ftc.enums.MatchType;
import com.maths22.ftc.repositories.MatchRepository;
import com.maths22.ftc.repositories.TeamEventAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class RankingsCalculator {
    private static final Logger _log = LoggerFactory.getLogger(RankingsCalculator.class);

    @Autowired
    TeamEventAssignmentRepository teamEventAssignmentRepository;

    @Autowired
    MatchRepository matchRepository;

    public List<Ranking> calculateRankings(Division division) {
        Map<Team, List<Ranking>> rankings = getRankingsForDivision(division);
        List<Ranking> ret = new ArrayList<>();
        for(Team team : rankings.keySet()) {
            List<Ranking> teamRankings = rankings.get(team);
            Ranking teamRanking;
            teamRanking = teamRankings.stream()
                    .reduce(new Ranking(team), this::mergeRankings);
            ret.add(teamRanking);
        }
        ret.sort(Collections.reverseOrder());
        return ret;
    }

    //TODO this limit approach is actually not what the real scoring system does
    public List<Ranking> calculateRankings(Collection<Team> teams, Collection<Division> divisions, int maxMatchCount) {
        Map<Team, ArrayList<Ranking>> rankings = divisions.stream()
                        .map(this::getRankingsForDivision)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .collect(Collectors.groupingBy(Map.Entry::getKey,
                                Collectors.mapping(Map.Entry::getValue,
                                        Collector.of(ArrayList::new, ArrayList::addAll, (a, b) -> {
                                            a.addAll(b);
                                            return a;
                                        })
                                )
                                ));
        List<Ranking> ret = new ArrayList<>();
        for(Team team : teams) {
            List<Ranking> teamRankings = rankings.get(team);
            Ranking teamRanking;
            teamRanking = teamRankings.stream()
                    .sorted(Collections.reverseOrder())
                    .limit(maxMatchCount)
                    .reduce(new Ranking(team), this::mergeRankings);
            ret.add(teamRanking);
        }
        ret.sort(Collections.reverseOrder());
        return ret;
    }


    private Ranking mergeRankings(Ranking r1, Ranking r2) {
        List<Integer> matchScores = new ArrayList<>();
        matchScores.addAll(r1.getMatchScores());
        matchScores.addAll(r2.getMatchScores());
        return new Ranking(r1.getTeam(), matchScores, r1.getQp() + r2.getQp(), r1.getRp() + r2.getRp());
    }

    private Map<Team, List<Ranking>> getRankingsForDivision(Division division) {
        List<TeamEventAssignment> teams = teamEventAssignmentRepository.findByDivisionId(division.getId());
        List<Match> matches = matchRepository.getByDivisionId(division.getId())
                .stream()
                .filter(m -> m.getType().equals(MatchType.QUALIFICATION))
                .filter(Match::isScored)
                .collect(Collectors.toList());
        Map<Team, List<Ranking>> rankingMap = new HashMap<>();
        for(TeamEventAssignment team : teams) {
            List<Match> teamMatches = matches.stream()
                    .filter(m -> m.getRedAlliance().getTeams().contains(team) || m.getBlueAlliance().getTeams().contains(team))
                    .collect(Collectors.toList());
            List<Ranking> rankings = teamMatches.stream()
                    .filter(m -> m.earnsQpRp(team))
                    .map(m -> m.computeRanking(team))
                    .collect(Collectors.toList());
            rankingMap.put(team.getTeam(), rankings);
        }
        return rankingMap;
    }
}
