package com.maths22.ftc.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Entity
public class Division {
    private UUID id;
    private int number;
    private String name;
    private Event event;
    private SortedSet<Match> matches;

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
    @Column(name = "number", nullable = false)
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

        Division division = (Division) o;

        if (number != division.number) return false;
        if (id != null ? !id.equals(division.id) : division.id != null) return false;
        return name != null ? name.equals(division.name) : division.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + number;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "event", referencedColumnName = "id", nullable = false)
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @OneToMany(mappedBy = "division")
    @SortNatural
    public SortedSet<Match> getMatches() {
        return matches;
    }

    public void setMatches(SortedSet<Match> matches) {
        this.matches = matches;
    }

}
