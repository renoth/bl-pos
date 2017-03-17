package de.renoth.blposition.domain;

public class TeamPossibility {
    private String teamName;

    private int maxPosition;

    private int currentPosition;

    private int minPosition;

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getMaxPosition() {
        return maxPosition;
    }

    public void setMaxPosition(int maxPosition) {
        this.maxPosition = maxPosition;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getMinPosition() {
        return minPosition;
    }

    public void setMinPosition(int minPosition) {
        this.minPosition = minPosition;
    }
}
