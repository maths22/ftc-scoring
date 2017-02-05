package com.maths22.ftc.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Entity
public class LeagueDivision {
    private UUID id;
    private String name;
    private League league;
    private List<Team> teams = new ArrayList<>();

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
    @Column(name = "name", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LeagueDivision league = (LeagueDivision) o;

        if (id != null ? !id.equals(league.id) : league.id != null) return false;
        return name != null ? name.equals(league.name) : league.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "league", referencedColumnName = "id", nullable = false)
    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    @Override
    public String toString() {
        return "events{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", league=" + league +
                '}';
    }

    @ManyToMany
    @JoinTable(name = "league_team", joinColumns = @JoinColumn(name = "league_division_id", referencedColumnName = "id", nullable = false), inverseJoinColumns = @JoinColumn(name = "team_id", referencedColumnName = "number", nullable = false))
    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }
}
