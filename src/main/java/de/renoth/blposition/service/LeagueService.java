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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private static final Logger LOG = LoggerFactory.getLogger(LeagueService.class);
    private static final int MAX_TRIES = 500000;
    private static final int LEAGUE_SIZE = 18;
    public static final int WIN_POINTS = 3;


    @PostConstruct
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

    public void calculatePossibleWorstPlacement() throws IOException {
        LOG.info("Calculate possible worst placements for league teams:");

        League league = getCurrentLeague();
        SortedSet<Team> teams = league.getTable().descendingSet();

        teams.stream().forEach(team -> {
            final int[] currentMatchday = {0};
            final int[] currentWorst = {0};
            final int[] tries = {0};

            team.setTested(true);

            League newLeague = SerializationUtils.clone(league);

            if (newLeague.getTeamPosition(team) == LEAGUE_SIZE) {
                LOG.info(team.getTeamName() + " is last, so worst placement is " + LEAGUE_SIZE);
                return;
            }

            newLeague.getTable().headSet(team, true).last().setTested(true);

            List<Match> openMatches = newLeague.getMatches().stream()
                    .filter(match -> match.getResult().equals(MatchResult.UNDECIDED))
                    .collect(Collectors.toList());

            List<Team> relevantTeams = new ArrayList<>();

            updateRelevantTeams(newLeague, relevantTeams, team, currentMatchday[0]);

            calculatePossibleOutcome(newLeague, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

            LOG.info(team.getTeamName() + " worst placement is " + currentWorst[0] + " after " + tries[0] + " tries");
        });

        league.updateTable();
    }

    private void calculatePossibleOutcome(League league, Team team, List<Match> openMatches, List<Team> relevantTeams, int[] currentMatchday, int[] currentWorst, int[] tries) {
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
            updateRelevantTeams(league, relevantTeams, team, currentMatchday[0]);

            if ((relevantTeams.size() + league.getTeamPosition(team)) <= currentWorst[0]) {
                openMatches.add(0, match);
                return;
            }
        }

        if (match.getHomeTeam().equals(team) || (relevantTeams.contains(match.getAwayTeam()) && !relevantTeams.contains(match.getHomeTeam()))) {
            match.setResult(MatchResult.AWAY_WIN);
            match.getAwayTeam().addPoints(WIN_POINTS);

            calculatePossibleOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

            match.setResult(MatchResult.UNDECIDED);
            match.getAwayTeam().addPoints(-WIN_POINTS);
        } else if (match.getAwayTeam().equals(team) || (relevantTeams.contains(match.getHomeTeam()) && !relevantTeams.contains(match.getAwayTeam()))) {
            match.setResult(MatchResult.HOME_WIN);
            match.getHomeTeam().addPoints(WIN_POINTS);

            calculatePossibleOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

            match.setResult(MatchResult.UNDECIDED);
            match.getHomeTeam().addPoints(-WIN_POINTS);
        } else {
            if (currentWorst[0] < LEAGUE_SIZE && tries[0] < MAX_TRIES && relevantTeams.contains(match.getHomeTeam()) && relevantTeams.contains(match.getAwayTeam())) {
                match.setResult(MatchResult.DRAW);
                match.getHomeTeam().addPoints(1);
                match.getAwayTeam().addPoints(1);

                calculatePossibleOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-1);
                match.getAwayTeam().addPoints(-1);

                match.setResult(MatchResult.HOME_WIN);
                match.getHomeTeam().addPoints(WIN_POINTS);

                calculatePossibleOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-WIN_POINTS);

                match.setResult(MatchResult.AWAY_WIN);
                match.getAwayTeam().addPoints(WIN_POINTS);

                calculatePossibleOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getAwayTeam().addPoints(-WIN_POINTS);
            } else {
                match.setResult(MatchResult.DRAW);
                match.getHomeTeam().addPoints(1);
                match.getAwayTeam().addPoints(1);

                calculatePossibleOutcome(league, team, openMatches, relevantTeams, currentMatchday, currentWorst, tries);

                match.setResult(MatchResult.UNDECIDED);
                match.getHomeTeam().addPoints(-1);
                match.getAwayTeam().addPoints(-1);
            }
        }

        openMatches.add(0, match);
    }

    public void updateRelevantTeams(League league, List<Team> relevantTeams, Team leadingTeam, int currentMatchday) {
        int MAX_MATCHDAY = 34;
        int maxPossiblePoints = (MAX_MATCHDAY - currentMatchday + 1) * WIN_POINTS;

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

        String json = getJsonFrom("http://www.openligadb.de/api/getmatchdata/bl1/2016");
        JavaType matchListType = mapper.getTypeFactory().constructCollectionType(List.class, Match.class);
        List<Match> matches = mapper.readValue(json, matchListType);

        return matches;
    }

    public void initializeTeams() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String json = getJsonFrom("http://www.openligadb.de/api/getavailableteams/bl1/2016");
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
