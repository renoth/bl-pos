package de.renoth.blposition.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.renoth.blposition.domain.League;
import de.renoth.blposition.domain.Match;
import de.renoth.blposition.domain.MatchResult;
import de.renoth.blposition.domain.Team;
import de.renoth.blposition.domain.deserializer.TeamDeserializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private static final Logger LOG = LoggerFactory.getLogger(LeagueService.class);
    private static final int MAX_TRIES = 1000000000;
    private static final int LEAGUE_SIZE = 18;
    private static final int WIN_POINTS = 3;

    private static League currentLeague;

    public Optional<League> getCalculatedLeague() {
        return Optional.ofNullable(currentLeague);
    }

    public League getCurrentLeague() throws IOException {
        List<Match> matches = getMatches();

        matches.parallelStream().forEach(match -> {
            if (match.getResult() == MatchResult.HOME_WIN) {
                match.getHomeTeam().addPoints(WIN_POINTS);
            } else if (match.getResult() == MatchResult.AWAY_WIN) {
                match.getAwayTeam().addPoints(WIN_POINTS);
            } else if (match.getResult() == MatchResult.DRAW) {
                match.getHomeTeam().addPoints(1);
                match.getAwayTeam().addPoints(1);
            }
        });

        League league = new League(TeamDeserializer.TEAMS.values());

        league.setMatches(matches);

        return league;
    }

    @PostConstruct
    public void calculatePossiblePlacements() throws IOException {
        LOG.info("Calculate possible worst placements for league teams:");

        League league = getCurrentLeague();
        SortedSet<Team> teams = league.getTable().descendingSet();

        teams.forEach(teamToTest -> {
            final int[] currentMatchday = {0};
            final int[] currentWorst = {0};
            final int[] tries = {0};

            Team team = null;

            League newLeague = SerializationUtils.clone(league);

            for (Team t : newLeague.getTable()) {
                if (teamToTest.equals(t)) {
                    team = t;
                    break;
                }
            }

            team.setTested(true);


            List<Match> openMatches = newLeague.getMatches().stream()
                    .filter(match -> match.getResult().equals(MatchResult.UNDECIDED))
                    .collect(Collectors.toList());

            List<Team> relevantTeams = new ArrayList<>();

            updateRelevantTeams(newLeague, relevantTeams, team, currentMatchday[0], false);

            calculatePossibleWorstOutcome(newLeague, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

            LOG.info(team.getTeamName() + " worst placement is " + currentWorst[0] + " after " + tries[0] + " tries");

            updateRelevantTeams(newLeague, relevantTeams, team, currentMatchday[0], true);

            int[] currentBest = {100};
            tries[0] = 0;

            team.setBestOutcome(true);

            calculatePossibleBestOutcome(newLeague, team, openMatches, relevantTeams, currentMatchday, currentBest, tries);

            LOG.info(team.getTeamName() + " best placement is " + currentBest[0] + " after " + tries[0] + " tries");

            teamToTest.setBestPostiion(currentBest[0]);
            teamToTest.setWorstPosition(currentWorst[0]);
        });

        league.updateTable();

        currentLeague = league;
    }

    private void calculatePossibleBestOutcome(League league, Team team, List<Match> openMatches, List<Team> relevantTeams, int[] currentMatchday, int[] currentBest, int[] tries) {
        if (openMatches.size() == 0) {
            tries[0]++;
            league.updateTable();
            currentBest[0] = Math.min(league.getTeamPosition(team), currentBest[0]);
            return;
        }

        Match match = openMatches.remove(0);

        if (currentMatchday[0] != match.getSpieltag()) {
            currentMatchday[0] = match.getSpieltag();
            league.updateTable();
            updateRelevantTeams(league, relevantTeams, team, currentMatchday[0], true);

            if ((league.getTeamPosition(team) - relevantTeams.size()) >= currentBest[0]) {
                tries[0]++;
                openMatches.add(0, match);
                return;
            }
        }

        if (match.getHomeTeam().equals(team) || (relevantTeams.contains(match.getAwayTeam()) && !relevantTeams.contains(match.getHomeTeam()))) {
            match.setResult(MatchResult.HOME_WIN);
            match.getHomeTeam().addPoints(WIN_POINTS);

            calculatePossibleBestOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentBest, tries);

            match.setResult(MatchResult.UNDECIDED);
            match.getHomeTeam().addPoints(-WIN_POINTS);
        } else if (match.getAwayTeam().equals(team) || (relevantTeams.contains(match.getHomeTeam()) && !relevantTeams.contains(match.getAwayTeam()))) {
            match.setResult(MatchResult.AWAY_WIN);
            match.getAwayTeam().addPoints(WIN_POINTS);

            calculatePossibleBestOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentBest, tries);

            match.setResult(MatchResult.UNDECIDED);
            match.getAwayTeam().addPoints(-WIN_POINTS);
        } else {
            if (currentBest[0] > 1 && tries[0] < MAX_TRIES && relevantTeams.contains(match.getHomeTeam()) && relevantTeams.contains(match.getAwayTeam())) {
                match.setResult(MatchResult.DRAW);
                match.getHomeTeam().addPoints(1);
                match.getAwayTeam().addPoints(1);

                calculatePossibleBestOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentBest, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-1);
                match.getAwayTeam().addPoints(-1);

                match.setResult(MatchResult.HOME_WIN);
                match.getHomeTeam().addPoints(WIN_POINTS);

                calculatePossibleBestOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentBest, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-WIN_POINTS);

                match.setResult(MatchResult.AWAY_WIN);
                match.getAwayTeam().addPoints(WIN_POINTS);

                calculatePossibleBestOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentBest, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getAwayTeam().addPoints(-WIN_POINTS);
            } else {
                match.setResult(MatchResult.DRAW);
                match.getHomeTeam().addPoints(1);
                match.getAwayTeam().addPoints(1);

                calculatePossibleBestOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentBest, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-1);
                match.getAwayTeam().addPoints(-1);
            }
        }

        openMatches.add(0, match);
    }

    private void calculatePossibleWorstOutcome(League league, Team team, List<Match> openMatches, List<Team> relevantTeams, int[] currentMatchday, int[] currentWorst, int[] tries) {
        if (openMatches.size() == 0) {
            tries[0]++;
            league.updateTable();
            currentWorst[0] = Math.max(league.getTeamPosition(team), currentWorst[0]);
            return;
        }

        Match match = openMatches.remove(0);

        if (currentMatchday[0] != match.getSpieltag()) {
            currentMatchday[0] = match.getSpieltag();
            league.updateTable();
            updateRelevantTeams(league, relevantTeams, team, currentMatchday[0], false);

            if ((relevantTeams.size() + league.getTeamPosition(team)) <= currentWorst[0]) {
                tries[0]++;
                openMatches.add(0, match);
                return;
            }
        }

        if (match.getHomeTeam().equals(team) || (relevantTeams.contains(match.getAwayTeam()) && !relevantTeams.contains(match.getHomeTeam()))) {
            match.setResult(MatchResult.AWAY_WIN);
            match.getAwayTeam().addPoints(WIN_POINTS);

            calculatePossibleWorstOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

            match.setResult(MatchResult.UNDECIDED);
            match.getAwayTeam().addPoints(-WIN_POINTS);
        } else if (match.getAwayTeam().equals(team) || (relevantTeams.contains(match.getHomeTeam()) && !relevantTeams.contains(match.getAwayTeam()))) {
            match.setResult(MatchResult.HOME_WIN);
            match.getHomeTeam().addPoints(WIN_POINTS);

            calculatePossibleWorstOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

            match.setResult(MatchResult.UNDECIDED);
            match.getHomeTeam().addPoints(-WIN_POINTS);
        } else {
            if (currentWorst[0] < LEAGUE_SIZE && tries[0] < MAX_TRIES && relevantTeams.contains(match.getHomeTeam()) && relevantTeams.contains(match.getAwayTeam())) {
                match.setResult(MatchResult.DRAW);
                match.getHomeTeam().addPoints(1);
                match.getAwayTeam().addPoints(1);

                calculatePossibleWorstOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-1);
                match.getAwayTeam().addPoints(-1);

                match.setResult(MatchResult.HOME_WIN);
                match.getHomeTeam().addPoints(WIN_POINTS);

                calculatePossibleWorstOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-WIN_POINTS);

                match.setResult(MatchResult.AWAY_WIN);
                match.getAwayTeam().addPoints(WIN_POINTS);

                calculatePossibleWorstOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getAwayTeam().addPoints(-WIN_POINTS);
            } else {
                match.setResult(MatchResult.DRAW);
                match.getHomeTeam().addPoints(1);
                match.getAwayTeam().addPoints(1);

                calculatePossibleWorstOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-1);
                match.getAwayTeam().addPoints(-1);
            }
        }

        openMatches.add(0, match);
    }

    public void updateRelevantTeams(League league, List<Team> relevantTeams, Team calculatedTeam, int currentMatchday, boolean isBestOutcome) {
        int MAX_MATCHDAY = 34;
        int maxPossiblePoints = (MAX_MATCHDAY - currentMatchday + 1) * WIN_POINTS;

        relevantTeams.clear();

        for (Team team : league.getTable()) {
            if (team.equals(calculatedTeam)) {
                continue;
            }

            if (!isBestOutcome) {
                if (calculatedTeam.getPoints() - team.getPoints() <= maxPossiblePoints && calculatedTeam.getPoints() > team.getPoints()) {
                    relevantTeams.add(team);
                }
            } else {
                if (team.getPoints() - calculatedTeam.getPoints() <= maxPossiblePoints && calculatedTeam.getPoints() < team.getPoints()) {
                    relevantTeams.add(team);
                }
            }
        }
    }

    private List<Match> getMatches() throws IOException {
        initializeTeams();

        ObjectMapper mapper = new ObjectMapper();

        String json = getJsonFrom("https://www.openligadb.de/api/getmatchdata/bl1/2016");
        JavaType matchListType = mapper.getTypeFactory().constructCollectionType(List.class, Match.class);

        return mapper.readValue(json, matchListType);
    }

    private void initializeTeams() throws IOException {
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
