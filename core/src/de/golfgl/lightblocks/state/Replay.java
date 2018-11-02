package de.golfgl.lightblocks.state;

import com.badlogic.gdx.utils.StringBuilder;

import java.util.ArrayList;
import java.util.LinkedList;

import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;

/**
 * Created by Benjamin Schulte on 31.10.2018.
 */

public class Replay {
    private static final char KEY_DROP_PIECE = 'D';
    private static final char KEY_ROTATE_PIECE = 'R';
    private static final char KEY_NEXT_PIECE = 'N';
    private static final char KEY_HORIZONTAL_MOVE = 'H';
    private static final char KEY_VERTICAL_MOVE = 'G';
    private static final char SEP_DEFAULT = ':';
    private static final char SEP_MAJOR = '#';
    private static final String KEY_VERSION = "1";
    private LinkedList<ReplayStep> replaySteps;
    private ArrayList<ReplayStep> arraySteps;
    private boolean isValid;
    private int currentReplayStepIdx;
    private int lastGameboardStepIdx;

    public Replay() {
        this.replaySteps = new LinkedList<ReplayStep>();
        isValid = true;
    }

    private static int findEndSeperator(int beginningPos, int maxPos, String toParse, char seperatorType) {
        int lastChar = Math.min(toParse.length() - 1, maxPos);
        for (int i = beginningPos; i <= lastChar; i++)
            if (toParse.charAt(i) == seperatorType)
                return i;

        return lastChar;
    }

    /**
     * verlässt den Replay-Mode
     */
    private void setChanged() {
        arraySteps = null;
    }

    /**
     * setzt den Replay-Mode: Umkopieren in Array, setzen der aktuellen Position
     */
    private void setReplayMode() {
        if (arraySteps == null) {
            arraySteps = new ArrayList<ReplayStep>(replaySteps);

            if (!isValid())
                arraySteps.clear();

            currentReplayStepIdx = -1;
            lastGameboardStepIdx = -1;
        }
    }

    public ReplayStep getFirstStep() {
        setReplayMode();

        currentReplayStepIdx = -1;
        return getNextStep();
    }

    public ReplayStep getPreviousStep() {
        setReplayMode();

        currentReplayStepIdx = Math.max(currentReplayStepIdx - 1, arraySteps.size());

        return getCurrentStep();
    }

    public ReplayStep getNextStep() {
        setReplayMode();

        currentReplayStepIdx = Math.min(currentReplayStepIdx + 1, arraySteps.size());

        return getCurrentStep();
    }

    public ReplayStep getCurrentStep() {
        setReplayMode();

        if (currentReplayStepIdx >= 0 && currentReplayStepIdx < arraySteps.size()) {
            ReplayStep replayStep = arraySteps.get(currentReplayStepIdx);
            if (replayStep instanceof ReplayGameboardStep)
                lastGameboardStepIdx = currentReplayStepIdx;
            else if (lastGameboardStepIdx > currentReplayStepIdx) {
                lastGameboardStepIdx = currentReplayStepIdx;
                while (lastGameboardStepIdx >= 0 && !(arraySteps.get(lastGameboardStepIdx) instanceof
                        ReplayGameboardStep)) {
                    lastGameboardStepIdx--;
                }
            }

            return replayStep;
        } else
            return null;
    }

    public ReplayGameboardStep getCurrentGameboardStep() {
        setReplayMode();

        if (lastGameboardStepIdx >= 0 && lastGameboardStepIdx < arraySteps.size())
            return (ReplayGameboardStep) arraySteps.get(lastGameboardStepIdx);
        else
            return null;
    }

    public boolean isValid() {
        return isValid;
    }

    public ReplayDropPieceStep addDropStep(int timeMs, Tetromino activeTetromino) {
        ReplayDropPieceStep currStep = new ReplayDropPieceStep();
        currStep.timeMs = timeMs;

        // das Aktive Piece speichern
        currStep.saveActivePiecePos(activeTetromino);

        replaySteps.add(currStep);
        setChanged();

        return currStep;
    }

    public void addNextPieceStep(int timeMs, Gameboard gameboard, Tetromino activeTetromino) {
        ReplayNextPieceStep currStep = new ReplayNextPieceStep();
        currStep.timeMs = timeMs;
        currStep.saveGameboard(gameboard);
        currStep.saveActivePiecePos(activeTetromino);
        replaySteps.add(currStep);
        setChanged();
    }

    public void addRotatePieceStep(int timeMs, Tetromino activeTetroMino) {
        RotateActivePieceStep currStep = new RotateActivePieceStep();
        currStep.timeMs = timeMs;
        currStep.saveActivePiecePos(activeTetroMino);
        replaySteps.add(currStep);
        setChanged();
    }

    public void addMovePieceStep(int timeMs, boolean horizontal, byte distance) {
        MovePieceStep currStep;
        if (horizontal)
            currStep = new HorizontalMovePieceStep();
        else
            currStep = new VerticalMovePieceStep();
        currStep.timeMs = timeMs;
        currStep.moveDistance = distance;
        replaySteps.add(currStep);
        setChanged();
    }

    @Override
    public String toString() {
        if (!isValid)
            return "NV#";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(KEY_VERSION);
        stringBuilder.append(SEP_MAJOR);
        int lastTimeStamp = 0;
        for (ReplayStep step : replaySteps) {
            step.appendTo(stringBuilder, lastTimeStamp);
            stringBuilder.append(SEP_MAJOR);
            lastTimeStamp = step.timeMs;
        }
        return stringBuilder.toString();
    }

    public boolean fromString(String toParse) {
        replaySteps.clear();
        isValid = false;
        setChanged();

        if (toParse == null || toParse.length() < 2)
            return false;

        int currentPos = findEndSeperator(0, toParse.length() - 1, toParse, SEP_MAJOR);

        String versionCode = toParse.substring(0, currentPos);
        if (!versionCode.equals(KEY_VERSION))
            return false;

        currentPos++;
        int lastTimeStamp = 0;

        while (toParse != null && currentPos < toParse.length()) {
            char type = toParse.charAt(currentPos);

            ReplayStep step;
            switch (type) {
                case KEY_DROP_PIECE:
                    ReplayDropPieceStep dropStep = new ReplayDropPieceStep();
                    step = dropStep;
                    break;
                case KEY_ROTATE_PIECE:
                    step = new RotateActivePieceStep();
                    break;
                case KEY_NEXT_PIECE:
                    step = new ReplayNextPieceStep();
                    break;
                case KEY_HORIZONTAL_MOVE:
                    step = new HorizontalMovePieceStep();
                    break;
                case KEY_VERTICAL_MOVE:
                    step = new VerticalMovePieceStep();
                    break;
                default:
                    return false;
            }

            currentPos++;

            int nextMajorSep = findEndSeperator(currentPos, toParse.length() - 1, toParse, SEP_MAJOR);

            step.fromString(currentPos, toParse, nextMajorSep, lastTimeStamp);
            replaySteps.add(step);
            lastTimeStamp = step.timeMs;

            currentPos = nextMajorSep + 1;
        }

        isValid = true;
        return isValid;
    }

    /**
     * Beliebiger Step, nur mit Timestamp
     */
    public static abstract class ReplayStep {
        public int timeMs;

        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            stringBuilder.append(Integer.toHexString(timeMs - lastTimeStamp));
            stringBuilder.append(SEP_DEFAULT);
        }

        public int fromString(int currentPos, String toParse, int maxPos, int lastTimeStamp) {
            int timeSeperator = findEndSeperator(currentPos, maxPos, toParse, SEP_DEFAULT);
            timeMs = lastTimeStamp + Integer.parseInt(toParse.substring(currentPos, timeSeperator), 16);
            currentPos = timeSeperator + 1;
            return currentPos;
        }
    }

    /**
     * Step mit Positionierung aktiver Block
     */
    public abstract static class ReplayActivePieceStep extends ReplayStep {
        public int[] activePiecePosition;

        public void saveActivePiecePos(Tetromino activeTetromino) {
            activePiecePosition = new int[Tetromino.TETROMINO_BLOCKCOUNT];
            Integer[][] currentBlockPositions = activeTetromino.getCurrentBlockPositions();
            for (byte blockNum = 0; blockNum < Tetromino.TETROMINO_BLOCKCOUNT; blockNum++) {
                activePiecePosition[blockNum] = currentBlockPositions[blockNum][0] +
                        Gameboard.GAMEBOARD_COLUMNS * currentBlockPositions[blockNum][1];
            }
        }

        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            super.appendTo(stringBuilder, lastTimeStamp);
            for (int i = 0; i < activePiecePosition.length; i++) {
                String str = Integer.toHexString(activePiecePosition[i]);
                if (str.length() < 2)
                    stringBuilder.append('0');
                stringBuilder.append(str);
            }
        }

        @Override
        public int fromString(int currentPos, String toParse, int maxPos, int lastTimeStamp) {
            currentPos = super.fromString(currentPos, toParse, maxPos, lastTimeStamp);
            activePiecePosition = new int[Tetromino.TETROMINO_BLOCKCOUNT];
            for (int i = 0; i < activePiecePosition.length; i++) {
                activePiecePosition[i] = Integer.parseInt(toParse.substring(currentPos, currentPos + 2), 16);
                currentPos += 2;
            }
            return currentPos;
        }
    }

    /**
     * Step mit Positionierung aktiver Block und vollem Spielfeld
     */
    public abstract static class ReplayGameboardStep extends ReplayActivePieceStep {
        public byte[] gameboard;

        public void saveGameboard(Gameboard gameboard) {
            int[][] gameboardSquares = gameboard.getGameboardSquares();
            this.gameboard = new byte[Gameboard.GAMEBOARD_ALLROWS * Gameboard.GAMEBOARD_COLUMNS];
            for (byte row = 0; row < Gameboard.GAMEBOARD_ALLROWS; row++) {
                for (byte column = 0; column < Gameboard.GAMEBOARD_COLUMNS; column++) {
                    this.gameboard[row * Gameboard.GAMEBOARD_COLUMNS + column] = (byte) gameboardSquares[row][column];
                }
            }
        }

        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            super.appendTo(stringBuilder, lastTimeStamp);
            stringBuilder.append(SEP_DEFAULT);
            int lastBlockFound = -1;
            for (int i = 0; i < gameboard.length; i++)
                if (gameboard[i] != Gameboard.SQUARE_EMPTY)
                    lastBlockFound = i;

            for (int i = 0; i <= lastBlockFound; i++) {
                stringBuilder.append(Gameboard.gameboardSquareToChar(gameboard[i]));
            }
        }

        @Override
        public int fromString(int currentPos, String toParse, int maxPos, int lastTimeStamp) {
            currentPos = super.fromString(currentPos, toParse, maxPos, lastTimeStamp);
            if (toParse.charAt(currentPos) == SEP_DEFAULT)
                currentPos += 1;

            gameboard = new byte[Gameboard.GAMEBOARD_ALLROWS * Gameboard.GAMEBOARD_COLUMNS];
            for (int i = 0; i < gameboard.length; i++)
                gameboard[i] = Gameboard.SQUARE_EMPTY;

            int i = 0;
            int endSeperator = findEndSeperator(currentPos, maxPos, toParse, SEP_DEFAULT);
            while (currentPos <= Math.min(maxPos, endSeperator) && i < gameboard.length) {
                char gameBoardChar = toParse.charAt(currentPos);
                if (gameBoardChar != SEP_DEFAULT && gameBoardChar != SEP_MAJOR) {
                    gameboard[i] = (byte) Gameboard.gameboardCharToSquare(gameBoardChar);
                    currentPos++;
                    i++;
                } else
                    i = gameboard.length;
            }
            return currentPos;
        }
    }

    /**
     * Nächstes Tetromino gezogen
     */
    public static class ReplayNextPieceStep extends ReplayGameboardStep {
        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            stringBuilder.append(KEY_NEXT_PIECE);
            super.appendTo(stringBuilder, lastTimeStamp);
        }
    }

    /**
     * Tetromino abgelegt, neuer Score ermittelt
     */
    public static class ReplayDropPieceStep extends ReplayActivePieceStep {
        public int score;

        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            stringBuilder.append(KEY_DROP_PIECE);
            super.appendTo(stringBuilder, lastTimeStamp);
            stringBuilder.append(SEP_DEFAULT);
            stringBuilder.append(Integer.toHexString(score));
        }

        @Override
        public int fromString(int currentPos, String toParse, int maxPos, int lastTimeStamp) {
            currentPos = super.fromString(currentPos, toParse, maxPos, lastTimeStamp);
            if (toParse.charAt(currentPos) == SEP_DEFAULT)
                currentPos += 1;
            int endSeperator = findEndSeperator(currentPos, maxPos, toParse, SEP_DEFAULT);
            score = Integer.parseInt(toParse.substring(currentPos, endSeperator), 16);
            return currentPos;
        }
    }

    /**
     * Tetromino gedreht
     */
    public static class RotateActivePieceStep extends ReplayActivePieceStep {
        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            stringBuilder.append(KEY_ROTATE_PIECE);
            super.appendTo(stringBuilder, lastTimeStamp);
        }
    }

    /**
     * Bewegung ohne Drehung
     */
    public abstract static class MovePieceStep extends ReplayStep {
        public int moveDistance;

        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            super.appendTo(stringBuilder, lastTimeStamp);
            stringBuilder.append(moveDistance);
        }

        @Override
        public int fromString(int currentPos, String toParse, int maxPos, int lastTimeStamp) {
            currentPos = super.fromString(currentPos, toParse, maxPos, lastTimeStamp);
            moveDistance = Integer.parseInt(toParse.substring(currentPos, maxPos));
            return maxPos;
        }

        public abstract int getMoveX();
        public abstract int getMoveY();
    }

    public static class HorizontalMovePieceStep extends MovePieceStep {
        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            stringBuilder.append(KEY_HORIZONTAL_MOVE);
            super.appendTo(stringBuilder, lastTimeStamp);
        }

        @Override
        public int getMoveX() {
            return moveDistance;
        }

        @Override
        public int getMoveY() {
            return 0;
        }
    }

    public static class VerticalMovePieceStep extends MovePieceStep {
        @Override
        public void appendTo(StringBuilder stringBuilder, int lastTimeStamp) {
            stringBuilder.append(KEY_VERTICAL_MOVE);
            super.appendTo(stringBuilder, lastTimeStamp);
        }

        @Override
        public int getMoveX() {
            return 0;
        }

        @Override
        public int getMoveY() {
            return moveDistance;
        }
    }
}
