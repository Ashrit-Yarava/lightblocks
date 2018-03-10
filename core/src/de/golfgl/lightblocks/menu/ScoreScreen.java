package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.AbstractMenuScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.IRoundScore;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Anzeige von Runden und Highscore
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class ScoreScreen extends AbstractMenuScreen {

    private static final int MAX_COUNTING_TIME = 2;
    private Array<IRoundScore> scoresToShow;
    private Array<String> scoresToShowLabels;
    private BestScore best;
    private String gameModelId;
    private InitGameParameters newGameParams;
    private Button leaveButton;
    private Actor defaultActor;

    private boolean newHighscore;

    public ScoreScreen(LightBlocksGame app) {
        super(app);

        scoresToShow = new Array<IRoundScore>();
        scoresToShowLabels = new Array<String>();
    }

    protected static String getFARatingString(int rating) {
        String scoreLabelString;
        scoreLabelString = "";
        rating--;

        for (int i = 0; i < 3; i++) {
            if (rating >= 2)
                scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_FULL;
            else if (rating >= 1)
                scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_HALF;
            else
                scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_EMPTY;

            rating = rating - 2;
        }
        return scoreLabelString;
    }

    public void initializeUI() {

        Table menuTable = new Table();
        fillMenuTable(menuTable);

        //Titel
        // Der Titel wird nach der Menütabelle gefüllt, eventuell wird dort etwas gesetzt (=> Scores)
        Label title = new Label(getTitle().toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        final String subtitle = getSubtitle();

        // Buttons
        Table buttons = new Table();
        buttons.defaults().uniform().expandX().center();

        // Back button
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        setBackButton(leaveButton);
        buttons.add(leaveButton);
        stage.addFocusableActor(leaveButton);
        stage.setEscapeActor(leaveButton);
        defaultActor = leaveButton;
        fillButtonTable(buttons);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.row().padTop(20);
        mainTable.add(new Label(getTitleIcon(), app.skin, FontAwesome.SKIN_FONT_FA));
        mainTable.row();
        mainTable.add(title);
        mainTable.row().expandY().top();
        if (subtitle != null)
            mainTable.add(new ScaledLabel(subtitle, app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        mainTable.row();
        mainTable.add(menuTable).width(LightBlocksGame.nativeGameWidth).pad(20);
        Button retryOrNext = addRetryOrNextButton();
        if (retryOrNext != null) {
            mainTable.row().expandY();
            mainTable.add(retryOrNext);
        }
        mainTable.row();
        mainTable.add(buttons).pad(20, 0, 20, 0).fillX();

        stage.addActor(mainTable);
    }

    protected String getTitleIcon() {
        if (scoresToShow.size >= 1 && scoresToShow.get(0).getRating() > 0)
            return getFARatingString(scoresToShow.get(0).getRating());
        else
            return FontAwesome.COMMENT_STAR_TROPHY;
    }

    public void addScoreToShow(IRoundScore score, String label) {
        scoresToShow.add(score);
        scoresToShowLabels.add(label);
    }

    /**
     * Nur für Highscore Erkennung zu füllen - ansonsten null lassen!
     *
     * @param best
     */
    public void setBest(BestScore best) {
        this.best = best;
    }

    public void setGameModelId(String gameModelId) {
        this.gameModelId = gameModelId;
    }

    protected String getSubtitle() {
        Mission mission = app.getMissionFromUid(gameModelId);
        String title = (mission != null ? app.TEXTS.format("labelMission", mission.getIndex())
                : app.TEXTS.get(Mission.getLabelUid(gameModelId)));

        return title;
    }

    protected String getTitle() {
        if (newHighscore)
            return app.TEXTS.get("motivationNewHighscore");
        else if (scoresToShowLabels.size == 1)
            return scoresToShowLabels.get(0);
        else
            return app.TEXTS.get("labelScores");
    }

    protected String getShareText() {
        return app.TEXTS.format((newHighscore ? "shareBestText" :
                "shareText"), scoresToShow.get(0).getScore(), LightBlocksGame.GAME_URL_SHORT, getSubtitle());
    }

    protected void fillMenuTable(Table menuTable) {

        ScoreTable scoreTable = new ScoreTable(app) {
            @Override
            protected boolean isBestScore(int i) {
                return (scoresToShow.get(i) instanceof BestScore);
            }
        };
        scoreTable.setMaxCountingTime(MAX_COUNTING_TIME);

        // Die Reihe mit den Labels
        if (scoresToShowLabels.size > 1) {
            scoreTable.add();

            for (int i = 0; i < scoresToShowLabels.size; i++)
                scoreTable.add(new Label(scoresToShowLabels.get(i).toUpperCase(), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG));
        }

        // SCORE
        Array<Long> scores = new Array<Long>(scoresToShow.size);
        for (int i = 0; i < scoresToShow.size; i++) {
            scores.add((long) scoresToShow.get(i).getScore());

            if (best != null && scoresToShow.get(i).getScore() >= best.getScore() && best.getScore() > 1000
                    && scoresToShow.get(i).getRating() >= best.getRating() && !scoreTable.isBestScore(i))
                newHighscore = true;
        }
        scoreTable.addScoresLine("labelScore", 8, scores, (best != null ? best.getScore() : 0));

        // LINES
        scores.clear();
        for (int i = 0; i < scoresToShow.size; i++)
            scores.add((long) scoresToShow.get(i).getClearedLines());

        scoreTable.addScoresLine("labelLines", 0, scores, (best != null ? best.getClearedLines() : 0));

        // BLOCKS
        scores.clear();
        for (int i = 0; i < scoresToShow.size; i++)
            scores.add((long) scoresToShow.get(i).getDrawnTetrominos());

        scoreTable.addScoresLine("labelBlocks", 0, scores, (best != null ? best.getDrawnTetrominos() : 0));

        menuTable.add(scoreTable);
    }

    protected void fillButtonTable(Table buttons) {
        // Share Button
        Button share = new ShareButton(app, getShareText());
        buttons.add(share);
        stage.addFocusableActor(share);

        // Leader Board
        final String leaderboardId = GpgsHelper.getLeaderBoardIdByModelId(gameModelId);
        if (leaderboardId != null) {
            Button leaderboard = new FaButton(FontAwesome.GPGS_LEADERBOARD, app.skin);
            leaderboard.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        app.gpgsClient.showLeaderboards(leaderboardId);
                    } catch (GameServiceException e) {
                        showDialog("Error showing leaderboard.");
                    }
                }
            });
            leaderboard.setDisabled(app.gpgsClient == null || !app.gpgsClient.isSessionActive());
            buttons.add(leaderboard);
            stage.addFocusableActor(leaderboard);

        }

    }

    private Button addRetryOrNextButton() {
        PlayButton retryOrNext = null;
        // Retry button
        if (newGameParams != null) {
            String retryOrNextIcon = FontAwesome.ROTATE_RIGHT;
            String retryOrNextLabel = "menuRetry";

            // Unterschied: Wenn Marathon oder Mission nicht geschafft, dann Retry
            // wenn aber Mission und Mission geschafft, dann nächste Mission anbieten!
            if (newGameParams.getMissionId() != null && scoresToShow.get(0).getRating() > 0) {
                int idxMissionDone = app.getMissionFromUid(newGameParams.getMissionId()).getIndex();

                // wenn wir bei der letzten Mission sind, kann man auch nix mehr machen
                if (app.getMissionList().size() > idxMissionDone + 1) {
                    newGameParams = new InitGameParameters();
                    newGameParams.setMissionId(app.getMissionList().get(idxMissionDone + 1).getUniqueId());
                    retryOrNextIcon = FontAwesome.BIG_PLAY;
                    retryOrNextLabel = "menuNextMission";
                }
            }

            retryOrNext = new PlayButton(app);
            retryOrNext.setText(app.TEXTS.get(retryOrNextLabel));
            retryOrNext.setFaText(retryOrNextIcon);
            retryOrNext.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        PlayScreen ps = PlayScreen.gotoPlayScreen(ScoreScreen.this, newGameParams);
                        ps.setBackScreen(ScoreScreen.this.backScreen);
                        dispose();
                    } catch (VetoException e) {
                        showDialog(e.getMessage());
                    }
                }
            });

            stage.addFocusableActor(retryOrNext);
            defaultActor = retryOrNext;
        }
        return retryOrNext;
    }

    public void setNewGameParams(InitGameParameters newGameParams) {
        this.newGameParams = newGameParams;
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);
        app.controllerMappings.setInputProcessor(stage);

        stage.setFocusedActor(defaultActor);

        swoshIn();

        // Wenn bereits 1000 Blöcke abgelegt sind und die Frage noch nicht verneint wurde bitten wir um ein Rating
        // das ganze zeitlich verzögert, damit der Bildschirm sauber aufgebaut ist
        if (!app.getDontAskForRating() && scoresToShow.size > 1 &&
                app.savegame.getTotalScore().getDrawnTetrominos() >= 1000)
            stage.getRoot().addAction(Actions.after(Actions.sequence(Actions.delay(.3f, Actions.run(new Runnable() {
                @Override
                public void run() {
                    askIfEnjoyingTheGame();
                }
            })))));
    }

    private void askIfEnjoyingTheGame() {
        showConfirmationDialog(app.TEXTS.get("labelAskForRating1"),
                new Runnable() {
                    @Override
                    public void run() {
                        //TODO focusedactor kommt durcheinander da der neue Dialog erst hochkommt und dann der alte geht
                        askForRating();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        app.setDontAskForRating(true);
                    }
                });
    }

    private void askForRating() {
        showConfirmationDialog(app.TEXTS.get("labelAskForRating2"),
                new Runnable() {
                    @Override
                    public void run() {
                        doRate();
                    }
                }, null, app.TEXTS.get("buttonIRateNow"), app.TEXTS.get("buttonRemindMeLater"));
    }

    private void doRate() {
        app.setDontAskForRating(true);
        Gdx.net.openURI(LightBlocksGame.GAME_STOREURL);
    }
}
