package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public class GameModel {
    // Für die Steuerung
    private static final float REPEAT_START_OFFSET = 0.5f;
    private static final float REPEAT_INTERVAL = 0.05f;
    private static final float SOFT_DROP_SPEED = 30.0f;
    // Speicherhaltung
    private final IntArray linesToRemove;
    IGameModelListener listener;
    TetrominoDrawyer drawyer;
    private Tetromino activeTetromino;
    private Tetromino nextTetromino;
    private Gameboard gameboard;
    // der aktuelle Punktestand
    private int score;
    // die abgebauten Reihen
    private int clearedLines;
    // Level
    private int level;
    // Fallgeschwindigkeit
    private float currentSpeed;
    // wieviel ist schon gefallen
    private float distanceRemainder;
    // Verzögerung bei gedrückter Taste
    private float movingCountdown;

    private float freezeCountdown;

    private boolean isGameOver;

    //vom Input geschrieben
    private boolean isSoftDrop;
    private int isRotate;
    private int isMovingLeft; // 0: nein, 1: ja, 2: gerade begonnen
    private int isMovingRight;

    public GameModel(IGameModelListener listener) {

        gameboard = new Gameboard();
        this.listener = listener;

        score = 0;
        linesToRemove = new IntArray(Gameboard.GAMEBOARD_ROWS);
        setClearedLines(0);

        activeTetromino = null;
        isGameOver = false;

        drawyer = new TetrominoDrawyer();
        nextTetromino = drawyer.getNextTetromino();
        activateNextTetromino();


    }

    public void update(float delta) {

        if (isGameOver) return;

        if (freezeCountdown > 0)
            freezeCountdown -= delta;

        if (freezeCountdown > 0)
            return;

        if (isRotate != 0) {
            rotate(isRotate > 0);
            isRotate = 0;
        }

        // horizontale Bewegung - nicht wenn beide Tasten gedrückt
        if (((isMovingLeft > 0) && (isMovingRight == 0)) ||
                (isMovingLeft == 0 && isMovingRight > 0)) {

            if (isMovingLeft >= 2 || isMovingRight >= 2) {
                movingCountdown = REPEAT_START_OFFSET;

                if (isMovingLeft > 0) {
                    moveHorizontal(-1);
                    isMovingLeft = 1;
                } else {
                    moveHorizontal(1);
                    isMovingRight = 1;
                }

            } else
                movingCountdown -= delta;

            if (movingCountdown <= 0.0f) {
                moveHorizontal(isMovingLeft > 0 ? -1 : 1);
                movingCountdown += REPEAT_INTERVAL;
            }
        }

        float speed = isSoftDrop ? SOFT_DROP_SPEED : currentSpeed;
        distanceRemainder += delta * speed;
        if (distanceRemainder >= 1.0f)
            moveDown((int) distanceRemainder);
    }

    private void moveDown(int distance) {
        int maxDistance = (-1) * gameboard.checkPossibleMoveDistance(false, -distance, activeTetromino);

        if (maxDistance > 0) {
            listener.moveBlocks(activeTetromino.getCurrentBlockPositions(), 0, -maxDistance);
            activeTetromino.getPosition().y -= maxDistance;
        }

        // wenn nicht bewegen konnte, dann festnageln und nächsten aktivieren
        if (maxDistance < distance) {

            gameboard.pinTetromino(activeTetromino);
            listener.playSound(IGameModelListener.SOUND_DROP);
            for (Integer[] vAfterMove : activeTetromino.getCurrentBlockPositions())
                listener.setBlockActivated(vAfterMove[0], vAfterMove[1], false);

            addDropScore();
            removeFullLines();

            // hiernach keine Zugriffe mehr auf activeTetromino!
            activateNextTetromino();
        } else {
            distanceRemainder -= distance;
        }
    }

    private void removeFullLines() {
        linesToRemove.clear();

        for (int i = 0; i < Gameboard.GAMEBOARD_ROWS; i++) {
            if (gameboard.isRowFull(i)) {
                linesToRemove.add(i);
            }
        }

        int lineCount = linesToRemove.size;
        if (lineCount == 0) {
            return;
        }

        gameboard.clearLines(linesToRemove);
        listener.clearLines(linesToRemove);
    }

    private void addDropScore() {
        //TODO
    }

    private void moveHorizontal(int distance) {
        int maxDistance = gameboard.checkPossibleMoveDistance(true, distance, activeTetromino);

        if (maxDistance != 0) {
            listener.moveBlocks(activeTetromino.getCurrentBlockPositions(), maxDistance, 0);
            activeTetromino.getPosition().x += maxDistance;
        }
    }

    private void rotate(boolean clockwise) {
        int newRotation = activeTetromino.getCurrentRotation() + (clockwise ? 1 : -1);

        if (gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                newRotation)) {

            // Die Position und auch die Einzelteile darin muss geclonet werden, um nicht
            // durch die Rotation verloren zu gehen
            Integer[][] oldBlockPositionsReference = activeTetromino.getCurrentBlockPositions();
            Integer[][] oldBlockPositionsNewArray = new Integer[oldBlockPositionsReference.length][2];
            for (int i = 0; i < oldBlockPositionsReference.length; i++) {
                oldBlockPositionsNewArray[i][0] = new Integer(oldBlockPositionsReference[i][0]);
                oldBlockPositionsNewArray[i][1] = new Integer(oldBlockPositionsReference[i][1]);
            }

            activeTetromino.setRotation(newRotation);

            listener.moveBlocks(oldBlockPositionsNewArray, activeTetromino.getCurrentBlockPositions());
            listener.playSound(IGameModelListener.SOUND_ROTATE);


        }
    }

    public void setFreezeInterval(float time) {
        freezeCountdown = time;
    }

    private void activateNextTetromino() {
        isSoftDrop = false;

        endMoveHorizontal(true);
        endMoveHorizontal(false);

        activeTetromino = nextTetromino;

        // ins Display damit
        for (Integer[] v : activeTetromino.getCurrentBlockPositions()) {
            listener.insertNewBlock(v[0], v[1]);
            listener.setBlockActivated(v[0], v[1], true);
        }

        distanceRemainder = 0.0f;
        nextTetromino = drawyer.getNextTetromino();

        // Wenn der neu eingefügte Tetromino keinen Platz mehr hat, ist das Spiel zu Ende
        if (!gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                activeTetromino.getCurrentRotation())) {
            isGameOver = true;
            listener.setGameOver(true);
        }

    }

    private void setClearedLines(int lines) {
        clearedLines = lines;
        level = 1 + clearedLines / 10;
        currentSpeed = 1.5f + (level - 1) * 0.25f;
        currentSpeed = Math.min(currentSpeed, SOFT_DROP_SPEED);
    }

    public void setSoftDrop(boolean newVal) {
        isSoftDrop = newVal;
    }

    public void setRotate(boolean clockwise) {
        isRotate = (clockwise ? 1 : -1);
    }

    public void startMoveHorizontal(boolean isLeft) {
        if (isLeft)
            isMovingLeft = 2;
        else
            isMovingRight = 2;
        movingCountdown = REPEAT_START_OFFSET;
    }

    public void endMoveHorizontal(boolean isLeft) {
        if (isLeft)
            isMovingLeft = 0;
        else
            isMovingRight = 0;

        movingCountdown = 0.0f;
    }

    public void fromPause() {
        // wenn während der Pause ein Knopf für Rotation gedrückt wurde, ist das
        // nicht zu beachten
        isRotate = 0;
    }
}
