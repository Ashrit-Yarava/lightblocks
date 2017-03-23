package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.MultiplayerModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.IRoomListener;
import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;
import de.golfgl.lightblocks.scenes.ScoreLabel;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Playscren für Multiplayerspiele um die Dinge einfach zu halten
 * <p>
 * Created by Benjamin Schulte on 01.03.2017.
 */

public class MultiplayerPlayScreen extends PlayScreen implements IRoomListener {

    private HashMap<String, ScoreLabel> playerLabels;
    private HashMap<String, GameBlocker.OtherPlayerPausedGameBlocker> playerBlockers = new HashMap<String,
            GameBlocker.OtherPlayerPausedGameBlocker>();
    private boolean isHandlingPauseMessage = false;

    public MultiplayerPlayScreen(LightBlocksGame app, InitGameParameters initGameParametersParams) throws
            InputNotAvailableException, VetoException {
        super(app, initGameParametersParams);

        // die Pause soll Anfangs nicht fest sein

    }

    @Override
    protected void populateScoreTable(Table scoreTable) {
        super.populateScoreTable(scoreTable);

        //TODO (eventuell) Bei disconnect haben die Clients keinen Zugriff mehr auf ihre Punkte die erreicht wurden
        // könnte man bei dem Event eventuell sonderbehandeln

        // Für die verschiedenen Spieler eine Zelle vorsehen. Noch nicht füllen, Infos stehen noch nicht zur Verfügung
        // das eingefügte ScoreLabel dient nur dazu den Platzbedarf festzulegen
        scoreTable.row();
        Label fillLabel = new Label(app.TEXTS.get("labelFill").toUpperCase(), app.skin);
        scoreTable.add(fillLabel).right().bottom().padBottom(3).spaceRight(3);

        // noch eine Tabelle für die Spieler
        Table fillingTable = new Table();
        playerLabels = new HashMap<String, ScoreLabel>(app.multiRoom.getNumberOfPlayers());

        for (String playerId : app.multiRoom.getPlayers()) {
            ScoreLabel lblFilling = new ScoreLabel(2, 100, app.skin, LightBlocksGame.SKIN_FONT_BIG);
            lblFilling.setExceedChar('X');
            fillingTable.add(lblFilling);
            playerLabels.put(playerId, lblFilling);
            fillingTable.add(new Label("%", app.skin)).padRight(10).bottom().padBottom(3);
        }

        scoreTable.add(fillingTable).colspan(3).align(Align.left);
    }

    @Override
    public void goBackToMenu() {
        if (!isPaused() && !gameModel.isGameOver())
            switchPause(false);

        else if (!((MultiplayerModel) gameModel).isCompletelyOver()) {
            //TODO hier vor dem Verlust der Ehre warnen und ob man wirklich möchte Ja/Nein

        } else {
            // ist eventuell doppelt, aber der unten im dispose kommt u.U. zu spät
            app.multiRoom.removeListener(this);
            super.goBackToMenu();
        }
    }

    @Override
    protected boolean goToScoresWhenOver() {
        return false;
    }

    @Override
    public void dispose() {
        app.multiRoom.removeListener(this);

        super.dispose();
    }

    @Override
    public void playersInGameChanged(MultiPlayerObjects.PlayerInGame pig) {
        ScoreLabel lblPlayerFill = playerLabels.get(pig.playerId);

        if (lblPlayerFill != null) {
            boolean notInitialized = (lblPlayerFill.getScore() == 100);
            lblPlayerFill.setScore(pig.filledBlocks * 100 / (Gameboard.GAMEBOARD_COLUMNS * Gameboard
                    .GAMEBOARD_NORMALROWS));

            if (notInitialized) {
                // geht nicht beim Init, da dieser mit 100 erfolgt und dann auf 0 zurückgesetzt wird
                lblPlayerFill.setEmphasizeTreshold(15, EMPHASIZE_COLOR);
                lblPlayerFill.setCountingSpeed(30);
                lblPlayerFill.setMaxCountingTime(.3f);
            }
        }
    }

    @Override
    public void switchPause(boolean immediately) {
        boolean oldIsPaused = isPaused();
        super.switchPause(immediately);

        // Pause gedrückt oder App in den Hintergrund gelegt... die anderen informieren
        if (!isHandlingPauseMessage && oldIsPaused != isPaused())
            sendPauseMessage(isPaused());
        else if (oldIsPaused)
            // Falls Pause gelöst werden sollte auch wenn nicht gelöst wurde senden, um Deadlock zu verhindern
            sendPauseMessage(false);
    }

    protected void sendPauseMessage(boolean nowPaused) {
        MultiPlayerObjects.SwitchedPause sp = new MultiPlayerObjects.SwitchedPause();
        sp.playerId = app.multiRoom.getMyPlayerId();
        sp.nowPaused = nowPaused;
        app.multiRoom.sendToAllPlayers(sp);
    }

    @Override
    public void pause() {
        super.pause();

        // Auf jeden Fall eine PauseMessage schicken!
        if (!gameModel.isGameOver())
            sendPauseMessage(true);
    }

    @Override
    public void removeGameBlocker(GameBlocker e) {
        // Abfrage vor remove, denn nach remove ja gerade nicht mehr
        boolean pausedByBlocker = !isGameBlockersEmpty();

        super.removeGameBlocker(e);

        //TODO hier sollte noch 3 Sekunden reingehen
        if (pausedByBlocker && isGameBlockersEmpty())
            switchPause(true);

    }

    @Override
    public void multiPlayerRoomStateChanged(AbstractMultiplayerRoom.RoomState roomState) {
        if (!roomState.equals(AbstractMultiplayerRoom.RoomState.inGame))
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (app.getScreen() == MultiplayerPlayScreen.this)
                        MultiplayerPlayScreen.super.goBackToMenu();
                }
            });
    }

    @Override
    public void multiPlayerRoomInhabitantsChanged(MultiPlayerObjects.PlayerChanged mpo) {
        //TODO anzeigen - deckt sich aber teilweise mit playersInGameChanged

        if (app.multiRoom.isOwner())
            ((MultiplayerModel) gameModel).handleMessagesFromOthers(mpo);
    }

    @Override
    public void multiPlayerGotErrorMessage(Object o) {
        //TODO anzeigen
    }

    @Override
    public void multiPlayerGotModelMessage(final Object o) {
        if (o instanceof MultiPlayerObjects.SwitchedPause)
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    handleOtherPlayerSwitchedPause((MultiPlayerObjects.SwitchedPause) o);
                }
            });

        //ansonsten weiter an das Spiel
        ((MultiplayerModel) gameModel).handleMessagesFromOthers(o);
    }

    private void handleOtherPlayerSwitchedPause(MultiPlayerObjects.SwitchedPause sp) {
        GameBlocker.OtherPlayerPausedGameBlocker pb = playerBlockers.get(sp.playerId);
        if (pb == null) {
            pb = new GameBlocker.OtherPlayerPausedGameBlocker();
            pb.playerId = sp.playerId;
            playerBlockers.put(sp.playerId, pb);
        }

        try {
            isHandlingPauseMessage = true;
            if (sp.nowPaused)
                addGameBlocker(pb);
            else
                removeGameBlocker(pb);
        } finally {
            isHandlingPauseMessage = false;
        }
    }

    @Override
    public void multiPlayerGotRoomMessage(Object o) {
        // bisher keine die hier zu verarbeiten sind.
    }
}
