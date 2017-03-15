package de.renoth.blposition.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.util.Comparator;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements Comparable<Team>, Serializable {
    @JsonProperty("ShortName")
    private String shortName;

    @JsonProperty("TeamName")
    private String teamName;

    @JsonProperty("TeamId")
    private Long id;

    private int points;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }


    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public int compareTo(Team otherTeam) {
        int result = otherTeam.points - points;

        if (result != 0) {
            return result;
        }

        return (int) (otherTeam.id - id);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Team) && ((Team) obj).getId() == id;
    }

    @Override
    public int hashCode() {
        return (int) (id * 23 + 1001);
    }

    @Override
    public String toString() {
        return teamName + " " + points;
    }
}
