package de.renoth.blposition.domain;

public enum MatchResult {
    HOME_WIN(1), AWAY_WIN(2), DRAW(0), UNDECIDED(-1);

    private final int i;

    MatchResult(int i) {
        this.i = i;
    }

    public int value() {
        return i;
    }
}
