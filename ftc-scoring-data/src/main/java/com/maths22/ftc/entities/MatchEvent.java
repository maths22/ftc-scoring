package com.maths22.ftc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maths22.ftc.enums.MatchEventType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Entity
public class MatchEvent {
    private UUID id;
    private MatchEventType type;
    private Match match;
    private TeamEventAssignment team;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    public MatchEventType getType() {
        return type;
    }

    public void setType(MatchEventType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchEvent that = (MatchEvent) o;

        return type == that.type;

    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "match", referencedColumnName = "id", nullable = false)
    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    @ManyToOne
    @JoinColumn(name = "team", referencedColumnName = "id", nullable = false)
    public TeamEventAssignment getTeam() {
        return team;
    }

    public void setTeam(TeamEventAssignment team) {
        this.team = team;
    }
}
