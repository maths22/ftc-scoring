package com.maths22.ftc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Entity
public class Alliance {
    private UUID id;
    private int seed;
    private boolean elimination;
    private Division division;
    private List<TeamEventAssignment> teams = new ArrayList<>();

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
    @Column(name = "seed", nullable = false)
    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Basic
    @Column(name = "elimination", nullable = false)
    public boolean isElimination() {
        return elimination;
    }

    public void setElimination(boolean elimination) {
        this.elimination = elimination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alliance alliance = (Alliance) o;

        if (seed != alliance.seed) return false;
        if (elimination != alliance.elimination) return false;
        return id != null ? id.equals(alliance.id) : alliance.id == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + seed;
        result = 31 * result + (elimination ? 1 : 0);
        return result;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division", referencedColumnName = "id", nullable = false)
    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @OrderColumn(name="team_index")
    @JoinTable(name = "alliance_team", joinColumns = @JoinColumn(name = "alliance_id", referencedColumnName = "id", nullable = false), inverseJoinColumns = @JoinColumn(name = "team_event_assignment_id", referencedColumnName = "id", nullable = false))
    @org.hibernate.annotations.Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    public List<TeamEventAssignment> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamEventAssignment> teams) {
        this.teams = teams;
    }
}
