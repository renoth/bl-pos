package de.renoth.blposition.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.renoth.blposition.domain.deserializer.TeamDeserializer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Match implements Serializable {
    @JsonProperty("MatchID")
    private Long id;

    @JsonProperty("Team1")
    @JsonDeserialize(using = TeamDeserializer.class)
    private Team homeTeam;

    @JsonProperty("Team2")
    @JsonDeserialize(using = TeamDeserializer.class)
    private Team awayTeam;

    private int spieltag;

    private int result;
    private int homeGoal;
    private int awayGoal;

    @JsonProperty("Group")
    private void unpackSpieltag(Map<String, String> group) {
        spieltag = Integer.parseInt(group.get("GroupOrderID"));
    }

    @JsonProperty("MatchResults")
    private void unpackResults(List<Map<String, String>> results) {
        if (results.size() == 0) {
            result = -1;
            homeGoal = -1;
            awayGoal = -1;
            return;
        }

        Map<String, String> finalResult = null;

        for (Map<String, String> result : results) {
            if (finalResult == null) {
                finalResult = result;
                continue;
            }

            if (Integer.parseInt(result.get("ResultOrderID")) > Integer.parseInt(finalResult.get("ResultOrderID"))) {
                finalResult = result;
            }
        }

        homeGoal = Integer.parseInt(finalResult.get("PointsTeam1"));
        awayGoal = Integer.parseInt(finalResult.get("PointsTeam2"));

        result = homeGoal > awayGoal ? 1 : (homeGoal < awayGoal ? 2 : 0);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getSpieltag() {
        return spieltag;
    }

    public void setSpieltag(int spieltag) {
        this.spieltag = spieltag;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getHomeGoal() {
        return homeGoal;
    }

    public void setHomeGoal(int homeGoal) {
        this.homeGoal = homeGoal;
    }

    public int getAwayGoal() {
        return awayGoal;
    }

    public void setAwayGoal(int awayGoal) {
        this.awayGoal = awayGoal;
    }

    @Override
    public String toString() {
        return homeTeam.getTeamName() + " - " + awayTeam.getTeamName() + " : " + result;
    }
}
