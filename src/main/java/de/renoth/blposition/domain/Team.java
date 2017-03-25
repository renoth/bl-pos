package de.renoth.blposition.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team implements Comparable<Team>, Serializable {
    @JsonProperty("TeamName")
    private String teamName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "TeamId")
    private Long id;

    private int points;

    @JsonIgnore
    private boolean isTested;

    @JsonIgnore
    private boolean bestOutcome;

    private int bestPostiion;

    private int worstPosition;

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
            return bestOutcome ? -1 : 1;
        } else if (otherTeam.isTested) {
            return otherTeam.bestOutcome ? 1 : -1;
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

    @JsonIgnore
    public boolean isTested() {
        return isTested;
    }

    public void setTested(boolean tested) {
        isTested = tested;
    }

    public void setBestOutcome(boolean bestOutcome) {
        this.bestOutcome = bestOutcome;
    }

    @JsonIgnore
    public boolean getIsBestOutcome() {
        return bestOutcome;
    }

    public int getBestPostiion() {
        return bestPostiion;
    }

    public void setBestPostiion(int bestPostiion) {
        this.bestPostiion = bestPostiion;
    }

    public int getWorstPosition() {
        return worstPosition;
    }

    public void setWorstPosition(int worstPosition) {
        this.worstPosition = worstPosition;
    }
}
