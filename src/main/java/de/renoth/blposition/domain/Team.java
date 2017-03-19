package de.renoth.blposition.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements Comparable<Team>, Serializable {
    @JsonProperty("TeamName")
    private String teamName;

    @JsonProperty("TeamId")
    private Long id;

    private int points;

    private boolean isTested;

    private boolean calculateBest;

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

        if (isTested) {
            return 1;
        } else if (otherTeam.isTested) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Team) && ((Team) obj).getId().equals(id);
    }

    @Override
    public int hashCode() {
        return (int) (id * 23 + 1001);
    }

    @Override
    public String toString() {
        return teamName + " " + points;
    }

    public void addPoints(int i) {
        points += i;
    }

    public boolean isTested() {
        return isTested;
    }

    public void setTested(boolean tested) {
        isTested = tested;
    }
}
