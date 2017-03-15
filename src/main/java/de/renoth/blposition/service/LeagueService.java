package de.renoth.blposition.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.renoth.blposition.domain.League;
import de.renoth.blposition.domain.Match;
import de.renoth.blposition.domain.Team;
import de.renoth.blposition.domain.deserializer.TeamDeserializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class LeagueService {

    @PostConstruct
    public League getCurrentLeague() throws IOException {
        List<Match> matches = getMatches();

        matches.stream().forEach(match -> {
            if (match.getResult() == 1) {
                match.getHomeTeam().setPoints(match.getHomeTeam().getPoints() + 3);
            } else if (match.getResult() == 2) {
                match.getAwayTeam().setPoints(match.getAwayTeam().getPoints() + 3);
            } else if (match.getResult() == 0) {
                match.getHomeTeam().setPoints(match.getHomeTeam().getPoints() + 1);
                match.getAwayTeam().setPoints(match.getAwayTeam().getPoints() + 1);
            }
        });

        League league = new League(TeamDeserializer.TEAMS.values());

        league.setMatches(matches);

        return league;
    }

    public void possibleWorstPlacementForLeadingTeam() throws IOException {
        League league = getCurrentLeague();
        Team leadingTeam = league.getTable().first();

        final int[] currentMatchday = {25};
        final int[] currentWorst = {0};

        int maximalPositionLoss = 0;

        int worstPosition = calculatePossibleOutcome(league, leadingTeam, currentMatchday, currentWorst);

        league.updateTable();
        //System.out.println(league);
    }

    private int calculatePossibleOutcome(League league, Team leadingTeam, int[] currentMatchday, int[] currentWorst) {
        List<Team> relevantTeams = new ArrayList<>();

        updateRelevantTeams(league, relevantTeams, leadingTeam, currentMatchday[0]);

        league.getMatches().stream()
                .filter(match -> match.getResult() < 0)
                .forEach(match -> {
                    if (match.getSpieltag() > currentMatchday[0]) {
                        //System.out.println("Neuer Spieltag " + currentMatchday[0]);

                        currentMatchday[0] = match.getSpieltag();
                        league.updateTable();
                        updateRelevantTeams(league, relevantTeams, leadingTeam, currentMatchday[0]);

                        //System.out.println("Noch Relevante Teams: " + relevantTeams.size());
                        //System.out.println(league);
                    }

                    if (match.getHomeTeam().equals(leadingTeam)) {
                        match.setResult(2);
                        match.getAwayTeam().setPoints(match.getAwayTeam().getPoints() + 3);
                    } else if (match.getAwayTeam().equals(leadingTeam)) {
                        match.setResult(1);
                        match.getHomeTeam().setPoints(match.getHomeTeam().getPoints() + 3);
                    } else {
                        if (relevantTeams.contains(match.getHomeTeam()) && relevantTeams.contains(match.getAwayTeam())) {
                            //System.out.println(match.getSpieltag());

                            match.setResult(1);
                            match.getHomeTeam().setPoints(match.getHomeTeam().getPoints() + 3);

                            League newLeague = SerializationUtils.clone(league);

                            int[] currentMdClone = new int[]{currentMatchday[0]};

                            currentWorst[0] = Math.max(calculatePossibleOutcome(newLeague, leadingTeam, currentMdClone, currentWorst), currentWorst[0]);

                            match.setResult(2);
                            match.getAwayTeam().setPoints(match.getAwayTeam().getPoints() + 3);

                            newLeague = SerializationUtils.clone(league);

                            currentMdClone = new int[]{currentMatchday[0]};

                            currentWorst[0] = Math.max(calculatePossibleOutcome(newLeague, leadingTeam, currentMdClone, currentWorst), currentWorst[0]);

                        } else if (relevantTeams.contains(match.getHomeTeam())) {
                            match.setResult(1);
                            match.getHomeTeam().setPoints(match.getHomeTeam().getPoints() + 3);
                        } else if (relevantTeams.contains(match.getAwayTeam())) {
                            match.setResult(2);
                            match.getAwayTeam().setPoints(match.getAwayTeam().getPoints() + 3);
                        } else {
                            match.setResult(0);
                            match.getHomeTeam().setPoints(match.getHomeTeam().getPoints() + 1);
                            match.getAwayTeam().setPoints(match.getAwayTeam().getPoints() + 1);
                        }
                    }
                });

        System.out.println(currentWorst[0]);

        league.updateTable();
        return league.getTeamPosition(leadingTeam);
    }

    private void updateRelevantTeams(League league, List<Team> relevantTeams, Team leadingTeam, int currentMatchday) {
        int MAX_MATCHDAY = 34;
        int maxPossiblePoints = (MAX_MATCHDAY - currentMatchday + 1) * 3;

        relevantTeams.clear();

        Iterator<Team> relevantTeamsIterator = league.getTable().iterator();

        while (relevantTeamsIterator.hasNext()) {
            Team team = relevantTeamsIterator.next();

            if (team.equals(leadingTeam)) {
                continue;
            }

            if (leadingTeam.getPoints() - team.getPoints() <= maxPossiblePoints && leadingTeam.getPoints() > team.getPoints()) {
                relevantTeams.add(team);
            }
        }
    }

    public List<Match> getMatches() throws IOException {
        initializeTeams();

        ObjectMapper mapper = new ObjectMapper();

        String json = getJsonFrom("https://www.openligadb.de/api/getmatchdata/bl1/2016");
        JavaType matchListType = mapper.getTypeFactory().constructCollectionType(List.class, Match.class);
        List<Match> matches = mapper.readValue(json, matchListType);

        return matches;
    }

    public void initializeTeams() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String json = getJsonFrom("https://www.openligadb.de/api/getavailableteams/bl1/2016");
        JavaType teamListType = mapper.getTypeFactory().constructCollectionType(List.class, Team.class);
        List<Team> teams = mapper.readValue(json, teamListType);

        TeamDeserializer.initialize(teams);
    }

    private String getJsonFrom(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        return IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
    }
}
