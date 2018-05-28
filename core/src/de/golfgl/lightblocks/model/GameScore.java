package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.state.IRoundScore;

/**
 * Diese Klasse verwaltet alle Punktstände eines Spiels oder der Gesamtspiele
 * <p>
 * Created by Benjamin Schulte on 29.01.2017.
 */

public class GameScore implements IRoundScore {
    protected static final int TYPE_NORMAL = 0;
    protected static final int TYPE_PRACTICE = 1;
    protected static final int TYPE_SPRINT = 2;

    // der aktuelle Punktestand
    private int score;
    // die abgebauten Reihen
    private int clearedLines;
    // Level
    private int startingLevel;
    // der noch nicht dem Gesamtscore zugerechnete aufgelaufene Score
    private float dropScore;
    // Anzahl gezogene Blöcke
    private int drawnTetrominos;
    private int rating;
    // vergangene Zeit
    private int seconds;
    private float secondFraction;
    private boolean lastClearLinesWasSpecial = false;

    private int scoringType = TYPE_NORMAL;

    @Override
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getLeaderboardScore() {
        switch (scoringType) {
            case TYPE_PRACTICE:
                return getDrawnTetrominos();
            case TYPE_SPRINT:
                return getTimeMs();
            default:
                return getScore();
        }
    }

    public String getLeaderboardTag() {
        switch (scoringType) {
            case TYPE_PRACTICE:
            case TYPE_SPRINT:
                return Integer.toString(getScore());
            default:
                return Integer.toString(getClearedLines());
        }
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public int getClearedLines() {
        return clearedLines;
    }

    /**
     * Berechnet und addiert den aktuellen Wert der abgebauten Reihen
     *
     * @param clearedLines Anzahl abgebaute Reihen
     * @param specialMove  Four Line oder T-Spin
     * @return true wenn specialmove und davor war auch special
     */
    public boolean incClearedLines(int clearedLines, boolean specialMove, boolean isTSpin) {
        // wiki/Scoring

        float removeScore;
        boolean doubleSpecial = specialMove && lastClearLinesWasSpecial;

        switch (clearedLines) {
            case 1:
                removeScore = (isTSpin ? 300 : 40);
                break;
            case 2:
                removeScore = (isTSpin ? 1200 : 100);
                break;
            case 3:
                removeScore = 300;
                break;
            case 4:
                removeScore = 1200;
                break;
            default:
                removeScore = 0;
        }

        removeScore = getCurrentScoreFactor() * removeScore;

        if (doubleSpecial)
            removeScore = removeScore * 1.5f;

        this.dropScore += removeScore;

        lastClearLinesWasSpecial = specialMove;

        this.clearedLines += clearedLines;

        return doubleSpecial;
    }

    protected int getCurrentScoreFactor() {
        if (scoringType == TYPE_PRACTICE || scoringType == TYPE_SPRINT)
            return 1;
        else
            return getCurrentLevel() + 1;
    }

    /**
     * returns current level depending on starting level and cleared lines
     */
    public int getCurrentLevel() {
        if (scoringType == TYPE_PRACTICE || scoringType == TYPE_SPRINT)
            return startingLevel;
        else
            return Math.max(startingLevel, clearedLines / 10);
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    public void addTSpinBonus() {
        this.dropScore += getCurrentScoreFactor() * 150;
    }

    public void addBonusScore(int bonusScore) {
        this.score += bonusScore;
    }

    public void addSoftDropScore(float softDropScore) {
        this.dropScore += softDropScore;
    }

    /**
     * sets the current dropscore to the gained score and returns it for displaying in user interface
     */
    public int flushScore() {
        int flushedScore = (int) dropScore;

        this.score += flushedScore;
        dropScore = 0;

        return flushedScore;
    }

    @Override
    public int getDrawnTetrominos() {
        return drawnTetrominos;
    }

    public int getTimeMs() {
        return seconds * 1000 + (int) (secondFraction * 1000f);
    }

    public void incDrawnTetrominos() {
        this.drawnTetrominos += 1;
    }

    public void incTime(float secondFraction) {
        this.secondFraction += secondFraction;
        while (this.secondFraction > 1f) {
            seconds++;
            this.secondFraction = this.secondFraction - 1f;
        }
    }

    public int getScoringType() {
        return scoringType;
    }

    protected void setScoringType(int scoringType) {
        this.scoringType = scoringType;
    }
}
