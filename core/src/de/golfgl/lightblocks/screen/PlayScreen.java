package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.BlockGroup;
import de.golfgl.lightblocks.scenes.MotivationLabel;
import de.golfgl.lightblocks.scenes.ParticleEffectActor;
import de.golfgl.lightblocks.scenes.ScoreLabel;
import de.golfgl.lightblocks.state.InitGameParameters;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * The main playing screen
 * <p>
 * Übernimmt auch den Adapter zwischen GameModel und Input/GUI
 * <p>
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class PlayScreen extends AbstractScreen implements IGameModelListener {

    private final BlockGroup blockGroup;
    private final Group labelGroup;
    private final BlockActor[][] blockMatrix;
    private final BlockActor[] nextTetro;
    private final ScoreLabel scoreNum;
    private final ScoreLabel levelNum;
    private final ScoreLabel linesNum;
    private final MotivationLabel motivatorLabel;
    private final ParticleEffectActor weldEffect;
    public GameModel gameModel;
    PlayScreenInput inputAdapter;
    Music music;
    float lastAccX = 0;
    private boolean isPaused = true;

    public PlayScreen(LightBlocksGame app, InitGameParameters initGameParametersParams) throws
            InputNotAvailableException, VetoException {
        super(app);

        ParticleEffect pweldEffect = new ParticleEffect();
        //TODO wenn Atlas da, dann hier ändern
        pweldEffect.load(Gdx.files.internal("raw/explode.p"), Gdx.files.internal("raw"));
        weldEffect = new ParticleEffectActor(pweldEffect);

        blockMatrix = new BlockActor[Gameboard.GAMEBOARD_COLUMNS][Gameboard.GAMEBOARD_ALLROWS];
        nextTetro = new BlockActor[Tetromino.TETROMINO_BLOCKCOUNT];

        // Die Blockgroup nimmt die Steinanimation auf
        blockGroup = new BlockGroup();
        blockGroup.setTransform(false);
        blockGroup.getColor().a = .4f;

        // 10 Steine breit, 20 Steine hoch
        blockGroup.setX((LightBlocksGame.nativeGameWidth - Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth) / 2);
        blockGroup.setY((LightBlocksGame.nativeGameHeight - (Gameboard.GAMEBOARD_ALLROWS) * BlockActor.blockWidth) /
                2);

        stage.addActor(blockGroup);

        // Begrenzungen um die BlockGroup
        final int ninePatchBorderSize = 5;
        NinePatch line = new NinePatch(app.trGlowingLine, ninePatchBorderSize, ninePatchBorderSize, ninePatchBorderSize,
                ninePatchBorderSize);
        Image imLine = new Image(line);

        imLine.setX(blockGroup.getX() + Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth);
        imLine.setY(blockGroup.getY() - ninePatchBorderSize);
        imLine.addAction(Actions.sizeTo(imLine.getWidth(), (Gameboard.GAMEBOARD_NORMALROWS) * BlockActor.blockWidth +
                2 * ninePatchBorderSize, 1f, Interpolation.circleOut));
        imLine.setColor(.8f, .8f, .8f, 1);
        stage.addActor(imLine);

        imLine = new Image(line);
        imLine.setY(blockGroup.getY() - ninePatchBorderSize);
        imLine.setX(blockGroup.getX() - imLine.getWidth() - 2);
        imLine.addAction(Actions.sizeTo(imLine.getWidth(), (Gameboard.GAMEBOARD_NORMALROWS) * BlockActor.blockWidth +
                2 * ninePatchBorderSize, 1f, Interpolation.circleOut));
        imLine.setColor(.8f, .8f, .8f, 1);
        stage.addActor(imLine);

        // Anzeige des Levels - muss in Group, Rotation funktioniert direkt auf Label nicht
        Group gameTypeLabels = new Group();
        Label gameType = new Label("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        gameType.setColor(.7f, .7f, .7f, 1);
        //gameType.setFontScale(.9f);
        gameTypeLabels.setPosition(imLine.getX() - gameType.getPrefHeight() / 2 - 5, blockGroup.getY());
        gameTypeLabels.addActor(gameType);
        gameTypeLabels.setRotation(90);
        //gameTypeLabels.addAction(Actions.rotateBy(90, 10f));

        stage.addActor(gameTypeLabels);

        // Score Labels
        final Table mainTable = new Table();

        mainTable.row();
        Label levelLabel = new Label(app.TEXTS.get("labelLevel").toUpperCase(), app.skin);
        mainTable.add(levelLabel).right().bottom().padBottom(3).spaceRight(3);
        levelNum = new ScoreLabel(2, 0, app.skin, LightBlocksGame.SKIN_FONT_BIG);
        mainTable.add(levelNum).left();
        Label linesLabel = new Label(app.TEXTS.get("labelLines").toUpperCase(), app.skin);
        mainTable.add(linesLabel).right().bottom().padBottom(3).spaceLeft(10);
        linesNum = new ScoreLabel(3, 0, app.skin, LightBlocksGame.SKIN_FONT_BIG);
        linesNum.setCountingSpeed(100);
        mainTable.add(linesNum).left();
        mainTable.row();
        Label scoreLabel = new Label(app.TEXTS.get("labelScore").toUpperCase(), app.skin);
        mainTable.add(scoreLabel).right().bottom().padBottom(3).spaceRight(3);
        scoreNum = new ScoreLabel(8, 0, app.skin, LightBlocksGame.SKIN_FONT_BIG);
        scoreNum.setCountingSpeed(2000);
        scoreNum.setMaxCountingTime(1);
        mainTable.add(scoreNum).left().colspan(3);

        mainTable.setY(LightBlocksGame.nativeGameHeight - mainTable.getPrefHeight() / 2 - 5);
        mainTable.setX(mainTable.getPrefWidth() / 2 + 5);

        stage.addActor(mainTable);

        stage.addActor(weldEffect);

        labelGroup = new Group();
        labelGroup.setTransform(false);
        labelGroup.setWidth(BlockActor.blockWidth * Gameboard.GAMEBOARD_COLUMNS);
        labelGroup.setHeight(BlockActor.blockWidth * Gameboard.GAMEBOARD_NORMALROWS - 2);
        labelGroup.setPosition(blockGroup.getX(), blockGroup.getY());
        stage.addActor(labelGroup);

        motivatorLabel = new MotivationLabel(app.skin, LightBlocksGame.SKIN_FONT_TITLE, labelGroup);

        initializeGameModel(initGameParametersParams);

        gameType.setText(app.TEXTS.get("labelModel_" + gameModel.getIdentifier()));

    }

    /**
     * Constructs a new game and sets the screen to it.
     *
     * @param caller        the AbstractScreen that is calling.
     * @param newGameParams null if game should be resumed.
     */
    public static PlayScreen gotoPlayScreen(AbstractScreen caller, InitGameParameters newGameParams) throws
            VetoException {

        boolean resumeGame = (newGameParams == null);

        if (System.currentTimeMillis() > LightBlocksGame.GAME_EXPIRATION)
            throw new VetoException("Sorry, this version of Lightblocks is outdated. Please download a newer version.");

        if (!resumeGame && caller.app.savegame.hasSavedGame())
            caller.app.savegame.resetGame();

        try {
            final PlayScreen currentGame;
            if (!resumeGame && newGameParams.isMultiplayer())
                currentGame = new MultiplayerPlayScreen(caller.app, newGameParams);
            else
                currentGame = new PlayScreen(caller.app, newGameParams);

            currentGame.setMusic(caller.app.isPlayMusic());

            Gdx.input.setInputProcessor(null);
            caller.app.setScreen(currentGame);

            return currentGame;

        } catch (InputNotAvailableException inp) {
            throw new VetoException(caller.app.TEXTS.get("errorInputNotAvail"));
        }
    }

    /**
     * returns if the game state is currently paused
     *
     * @return true if is paused, false if not
     */
    public boolean isPaused() {
        return isPaused;
    }

    protected void initializeGameModel(InitGameParameters initGameParametersParams) throws InputNotAvailableException,
            VetoException {
        // Game Model erst hinzufügen, wenn die blockgroup schon steht
        if (initGameParametersParams == null && !app.savegame.hasSavedGame()) {
            throw new VetoException("No savegame available!");
        } else if (initGameParametersParams == null) {
            Json json = new Json();
            gameModel = json.fromJson(GameModel.class, app.savegame.loadGame());
        } else {
            try {
                gameModel = initGameParametersParams.getGameModelClass().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Given game model class is not appropriate.", e);
            }
            gameModel.startNewGame(initGameParametersParams);
        }

        gameModel.setUserInterface(this);

        inputAdapter = PlayScreenInput.getPlayInput(gameModel.inputTypeKey);
        inputAdapter.setPlayScreen(this);
        inputAdapter.showHelp(labelGroup, true);

        // Highscores
        gameModel.totalScore = app.savegame.loadTotalScore();
        //TODO das sollte ins GameModel
        gameModel.bestScore = app.savegame.loadBestScore(gameModel.getIdentifier());

        // erst nach dem Laden setzen, damit das noch ohne Animation läuft
        levelNum.setEmphasizeTreshold(1, new Color(1, .3f, .3f, 1));
        scoreNum.setEmphasizeTreshold(1000, new Color(1, .3f, .3f, 1));

    }

    @Override
    public void render(float delta) {

        // Controller und Schwerkraft müssen gepollt werden
        inputAdapter.doPoll(delta);

        delta = Math.min(delta, 1 / 30f);

        if (!isPaused)
            gameModel.update(delta);

        super.render(delta);

    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(inputAdapter);
        swoshIn();
    }

    @Override
    public void goBackToMenu() {

        if (gameModel.isGameOver())
            goToHighscores();

        else {
            saveGameState();
            super.goBackToMenu();
        }
    }

    @Override
    public void dispose() {
        setMusic(false);
        weldEffect.dispose();
        super.dispose();
    }

    private void saveGameState() {
        if (app.savegame.canSaveState()) {
            app.savegame.saveTotalScore(gameModel.totalScore);
            app.savegame.saveBestScore(gameModel.bestScore, gameModel.getIdentifier());
            app.savegame.saveGame(gameModel.saveGameModel());
        }
    }

    @Override
    public void pause() {
        super.pause();

        if (!isPaused && !gameModel.isGameOver())
            switchPause(true);
    }

    public void switchPause(boolean immediately) {

        if (gameModel.isGameOver())
            goToHighscores();

        else {
            isPaused = !isPaused;

            final float fadingInterval = immediately ? 0 : .2f;

            //inform input adapter, too
            inputAdapter.isPaused = isPaused;

            blockGroup.clearActions();

            if (!isPaused) {

                if (music != null)
                    music.play();

                if (blockGroup.getColor().a < 1) {
                    blockGroup.addAction(Actions.fadeIn(fadingInterval));
                    gameModel.setFreezeInterval(fadingInterval);
                }

                labelGroup.clearChildren();

                //inform the game model that there was a pause
                gameModel.fromPause();
            } else {
                blockGroup.addAction(Actions.fadeOut(fadingInterval));
                if (music != null)
                    music.pause();

                // Spielstand speichern
                saveGameState();

                inputAdapter.showHelp(labelGroup, false);
            }
        }
    }

    private void goToHighscores() {

        ScoreScreen scoreScreen = new ScoreScreen(app);
        scoreScreen.setGameModelId(gameModel.getIdentifier());
        scoreScreen.addScoreToShow(gameModel.getScore(), app.TEXTS.get("labelRoundScore"));
        if (app.savegame.canSaveState()) {
            scoreScreen.setBest(gameModel.bestScore);
            scoreScreen.addScoreToShow(gameModel.bestScore, app.TEXTS.get("labelBestScore"));
        }
        scoreScreen.setNewGameParams(gameModel.getInitParameters());
        scoreScreen.setMaxCountingTime(2);
        scoreScreen.setBackScreen(this.backScreen);
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);

        this.dispose();
    }

    @Override
    public void insertNewBlock(int x, int y) {
        BlockActor block = new BlockActor(app);
        insertBlock(x, y, block);
    }

    private void insertBlock(int x, int y, BlockActor block) {
        block.setX(x * BlockActor.blockWidth);
        block.setY(y * BlockActor.blockWidth);
        blockGroup.addActor(block);
        blockMatrix[x][y] = block;
    }

    @Override
    public void moveTetro(Integer[][] v, int dx, int dy) {
        if (dx != 0 || dy != 0) {
            // erst alle vom Spielbrett einsammeln...
            Array<BlockActor> blocks = removeBlockActorsFromMatrix(v);

            //... und dann neu ablegen
            for (int i = 0; i < v.length; i++) {
                BlockActor block = blocks.get(i);
                int x = v[i][0];
                int y = v[i][1];
                block.setMoveAction(Actions.moveTo((x + dx) * BlockActor.blockWidth, (y + dy) * BlockActor
                        .blockWidth, 1 / 30f));
                blockMatrix[x + dx][y + dy] = block;
            }
        }
    }

    private Array<BlockActor> removeBlockActorsFromMatrix(Integer[][] v) {
        Array<BlockActor> blocks = new Array<BlockActor>(v.length);

        for (Integer[] xy : v) {
            if (blockMatrix[xy[0]][xy[1]] == null)
                Gdx.app.error("BLOCKS", "Block null at " + xy[0].toString() + " " + xy[1].toString());

            blocks.add(blockMatrix[xy[0]][xy[1]]);
            blockMatrix[xy[0]][xy[1]] = null;
        }
        return blocks;
    }

    @Override
    public void rotateTetro(Integer[][] vOld, Integer[][] vNew) {
        app.rotateSound.play();

        // erst alle vom Spielbrett einsammeln...
        Array<BlockActor> blocks = removeBlockActorsFromMatrix(vOld);

        //... und dann neu ablegen
        for (int i = 0; i < vOld.length; i++) {
            BlockActor block = blocks.get(i);
            int newx = vNew[i][0];
            int newy = vNew[i][1];
            block.setMoveAction(Actions.moveTo((newx) * BlockActor.blockWidth, (newy) * BlockActor.blockWidth, 1 /
                    20f));
            blockMatrix[newx][newy] = block;
        }


    }

    @Override
    public void clearLines(IntArray linesToRemove, boolean special) {

        final float removeDelayTime = .15f;
        final float removeFadeOutTime = .2f;
        final float moveActorsTime = .1f;

        gameModel.setFreezeInterval(removeDelayTime);

        // Vorbereitung zum Heraussuchen der Zeilen, die welche ersetzen
        IntArray lineMove = new IntArray(Gameboard.GAMEBOARD_ALLROWS);
        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++)
            lineMove.add(i);


        app.removeSound.play(.4f + linesToRemove.size * .2f);
        if (special)
            app.cleanSpecialSound.play(.8f);

        for (int i = linesToRemove.size - 1; i >= 0; i--) {
            int y = linesToRemove.get(i);

            // die zu entfernende Zeile durchgehen und alle Blöcke erleuchten
            // und entfernen
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                BlockActor block = blockMatrix[x][y];
                blockMatrix[x][y] = null;
                block.setEnlightened(true);


                if (special)
                    // Spezialeffekt: Verdichtung auf einen Block
                    block.setMoveAction(Actions.moveTo(4.5f * BlockActor.blockWidth, (linesToRemove.get(0) - .5f +
                            linesToRemove.size / 2) * BlockActor.blockWidth, removeDelayTime, Interpolation.fade));
                else if (linesToRemove.size >= 3)
                    // ab 3 Zeilen alle zeilenweise zusammen schieben
                    block.setMoveAction(Actions.moveTo(4.5f * BlockActor.blockWidth, (linesToRemove.get(i)) *
                            BlockActor.blockWidth, removeDelayTime, Interpolation.fade));
                else if (y == i)
                    // die untersten zusammenhängenden Zeilen rausschieben
                    block.setMoveAction(sequence(Actions.delay(removeDelayTime), Actions.moveBy(0, -(i + 1) *
                            BlockActor.blockWidth, moveActorsTime)));

                block.addAction(sequence(Actions.delay(removeDelayTime), Actions.fadeOut(removeFadeOutTime),
                        Actions.removeActor()));
            }

            // heraussuchen durch weile Zeile diese hier ersetzt wird
            for (int higherY = y; higherY < Gameboard.GAMEBOARD_ALLROWS; higherY++)
                if (higherY < Gameboard.GAMEBOARD_ALLROWS - 1)
                    lineMove.set(higherY, lineMove.get(higherY + 1));
                else
                    lineMove.set(higherY, -1);

        }

        // den Explosions-Effekt einfügen
        if (special) {
            weldEffect.setPosition(blockGroup.getX() + 5f * BlockActor.blockWidth, blockGroup.getY() +
                    (linesToRemove.size / 2 + linesToRemove.get(0)) * BlockActor.blockWidth);
            weldEffect.start();
        }

        for (int i = 0; i < lineMove.size; i++) {
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {

                if (lineMove.get(i) >= 0) {
                    BlockActor block = blockMatrix[x][lineMove.get(i)];
                    blockMatrix[x][lineMove.get(i)] = null;
                    blockMatrix[x][i] = block;
                    if (block != null)
                        block.setMoveAction(sequence(Actions.delay(removeDelayTime), (Actions.moveTo((x) *
                                BlockActor.blockWidth, (i) * BlockActor.blockWidth, moveActorsTime))));
                }

            }
        }


    }

    @Override
    public void setGameOver() {
        if (music != null)
            music.stop();
        app.gameOverSound.play();
        inputAdapter.setGameOver();
        saveGameState();
    }

    @Override
    public void showNextTetro(Integer[][] relativeBlockPositions) {
        // ein neuer nächster-Stein wurde bestimmt. Wir zeigen ihn einfach über dem Spielfeld an
        // Er wird aber zunächst nicht der Blockgroup hinzugefügt, damit er wenn er einfliegt keine Steine auf dem
        // Spielfeld überlagert

        final float offsetX = LightBlocksGame.nativeGameWidth - blockGroup.getX() - (Tetromino.TETROMINO_BLOCKCOUNT -
                .3f) * BlockActor
                .blockWidth;
        final float offsetY = (Gameboard.GAMEBOARD_NORMALROWS + .3f) * BlockActor.blockWidth;

        for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
            nextTetro[i] = new BlockActor(app);
            nextTetro[i].setPosition((i == 0 || i == 2) ? -BlockActor.blockWidth : LightBlocksGame.nativeGameWidth +
                            BlockActor.blockWidth,
                    (i >= 2) ? 0 : LightBlocksGame.nativeGameHeight);
            nextTetro[i].setMoveAction(Actions.moveTo(offsetX + relativeBlockPositions[i][0] * BlockActor.blockWidth,
                    offsetY + relativeBlockPositions[i][1] * BlockActor.blockWidth, .5f, Interpolation.fade));
            nextTetro[i].addAction(Actions.alpha(.5f, .5f, Interpolation.fade));
            nextTetro[i].getColor().a = 0;

            blockGroup.addActorAt(0, nextTetro[i]);
        }
    }

    @Override
    public void activateNextTetro(Integer[][] boardBlockPositions) {

        for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
            // den bereits in nextTetro instantiierten Block ins Spielfeld an die gewünschte Stelle bringen
            BlockActor block = nextTetro[i];

            final int x = boardBlockPositions[i][0];
            final int y = boardBlockPositions[i][1];

            if (block == null) {
                //beim Spielstart noch nicht gesetzt und die Animation macht auch keinen Sinn,
                //dann gleich an Zielposition instanziieren
                block = new BlockActor(app);
                insertBlock(x, y, block);
            } else {
                nextTetro[i] = null;
                blockMatrix[x][y] = block;
                block.addAction(Actions.fadeIn(.1f));
                block.setMoveAction(Actions.moveTo(x * BlockActor.blockWidth, y * BlockActor.blockWidth, .1f,
                        Interpolation.fade));
            }
            block.setEnlightened(true);
        }
    }

    @Override
    public void pinTetromino(Integer[][] currentBlockPositions) {
        app.dropSound.play();
        for (Integer[] vAfterMove : currentBlockPositions)
            blockMatrix[vAfterMove[0]][vAfterMove[1]].setEnlightened(false);
    }

    @Override
    public void markConflict(int x, int y) {
        BlockActor block = blockMatrix[x][y];
        block.showConflictTouch();
    }

    @Override
    public void showMotivation(MotivationTypes achievement, String extraMsg) {

        boolean playSound = true;
        String text = "";
        float duration = 2;

        switch (achievement) {
            case newLevel:
                text = app.TEXTS.get("labelLevel") + " " + extraMsg;
                break;
            case tSpin:
                text = app.TEXTS.get("motivationTSpin");
                break;
            case doubleSpecial:
                text = app.TEXTS.get("motivationDoubleSpecial");
                break;
            case tenLinesCleared:
                text = extraMsg + " " + app.TEXTS.get("labelLines");
                break;
            case newHighscore:
                text = app.TEXTS.get("motivationNewHighscore");
                break;
            case hundredBlocksDropped:
                text = app.TEXTS.format("motivationHundredBlocks", extraMsg);
                playSound = false;
                break;
            case playerOver:
                text = app.TEXTS.format("motivationPlayerOver", extraMsg);
                break;
            case gameOver:
                text = app.TEXTS.format("motivationGameOver");
                duration = 10;
                playSound = false;
                break;
            case gameWon:
                text = app.TEXTS.format("motivationGameWon");
                duration = 10;
                playSound = false;
                break;
        }

        if (playSound)
            app.unlockedSound.play();

        if (!text.isEmpty())
            motivatorLabel.addMotivationText(text.toUpperCase(), duration);
    }

    @Override
    public void updateScore(GameScore score, int gainedScore) {
        linesNum.setScore(score.getClearedLines());
        levelNum.setScore(score.getCurrentLevel());
        scoreNum.setScore(score.getScore());
    }

    @Override
    public void playersInGameChanged() {
        // Das passiert nur beim Multiplayer daher hier nichts
    }

    public void setMusic(boolean playMusic) {
        if (playMusic) {
            music = Gdx.audio.newMusic(Gdx.files.internal("sound/dd.ogg"));
            music.setVolume(1f);                 // sets the volume to half the maximum volume
            music.setLooping(true);
        } else if (music != null)
            music.dispose();

    }


}
