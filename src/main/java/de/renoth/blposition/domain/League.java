package de.renoth.blposition.domain;

import java.io.Serializable;
import java.util.*;

public class League implements Serializable {
    private TreeSet<Team> table;

    private List<Match> matches;

    public League(Collection<Team> teams) {
        this.table = new TreeSet<>();
        table.addAll(teams);
    }

    public TreeSet<Team> getTable() {
        return table;
    }

    public void setTable(TreeSet<Team> table) {
        this.table = table;
    }

    public void updateTable() {
        Iterator<Team> teams = table.iterator();
        List<Team> teamsToReinsert = new ArrayList<>();

        while (teams.hasNext()) {
            teamsToReinsert.add(teams.next());
            teams.remove();
        }

        table.clear();

        table.addAll(teamsToReinsert);
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        for (Team team : table.tailSet(table.first())) {
            sb.append(team.toString() + "\n");
        }

        return sb.toString();
    }

    public int getTeamPosition(Team leadingTeam) {
        return table.headSet(leadingTeam, true).size();
    }
}
