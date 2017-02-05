package com.maths22.ftc.ranking;

import com.maths22.ftc.entities.Team;
import com.maths22.ftc.entities.TeamEventAssignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jacob on 9/27/2016.
 */
public class Ranking implements Comparable<Ranking> {
    private final Team team;
    private final List<Integer> matchScores;
    private final int qp;
    private final int rp;

    private final List<Integer> sortedMatchScores;

    public Ranking(Team team, List<Integer> matchScores, int qp, int rp) {
        this.team = team;
        this.matchScores = matchScores;
        this.qp = qp;
        this.rp = rp;

        this.sortedMatchScores = new ArrayList<>(this.matchScores);
        sortedMatchScores.sort(Collections.reverseOrder());
    }

    public Team getTeam() {
        return team;
    }

    public int getQp() {
        return qp;
    }

    public int getRp() {
        return rp;
    }

    public List<Integer> getMatchScores() {
        return matchScores;
    }

    public List<Integer> getSortedMatchScores() {
        return sortedMatchScores;
    }

    @Override
    public int compareTo(Ranking o) {
        int res = Integer.compare(this.qp, o.qp);
        if(res == 0) {
            res = Integer.compare(this.rp, o.rp);
        }
        if(res == 0) {
            //TODO this does not match the official system (only compares top score)
            //However, this does match the game manual
            for(int i = 0; i < Math.min(this.sortedMatchScores.size(), o.sortedMatchScores.size()); i++) {
                res = Integer.compare(this.sortedMatchScores.get(i), o.sortedMatchScores.get(i));
                if(res != 0) {
                    return res;
                }
            }
        }
        if(res == 0) {
            //todo make more random
            res = Integer.compare(this.team.hashCode(), o.team.hashCode());
        }
        return res;
    }
}
//TODO write tests above^