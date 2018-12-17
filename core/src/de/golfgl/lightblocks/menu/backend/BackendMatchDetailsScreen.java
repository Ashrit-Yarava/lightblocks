package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.menu.PlayButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 16.12.2018.
 */

public class BackendMatchDetailsScreen extends WaitForBackendFetchDetailsScreen<String, MatchEntity> {
    private final Button playTurnButton;
    private final Button resignButton;
    private MatchEntity match;

    public BackendMatchDetailsScreen(LightBlocksGame app, String matchId) {
        super(app, matchId);

        playTurnButton = new PlayButton(app);
//                =new RoundedTextButton("P", app.skin);
        playTurnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO das eigentliche Spiel beginnen
            }
        });
        addFocusableActor(playTurnButton);

        resignButton = new FaTextButton("Resign", app.skin, LightBlocksGame.SKIN_BUTTON_CHECKBOX);
        resignButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO aufgeben
            }
        });

        // TODO Akzeptieren/Ablehnen button
    }

    @Override
    protected void fillFixContent() {
        Table contentTable = getContentTable();
        contentTable.row();
        contentTable.add(new Label(FontAwesome.NET_PEOPLE, app.skin, FontAwesome.SKIN_FONT_FA));
    }

    protected void reload() {
        contentCell.setActor(waitRotationImage);
        //TODO über den Manager gehen und cachen
        app.backendManager.getBackendClient().fetchMatchWithTurns(backendId, new BackendManager
                .AbstractQueuedBackendResponse<MatchEntity>(app) {

            @Override
            public void onRequestFailed(final int statusCode, final String errorMsg) {
                fillErrorScreen(statusCode, errorMsg);
            }

            @Override
            public void onRequestSuccess(final MatchEntity retrievedData) {
                fillMatchDetails(retrievedData);
            }
        });
    }

    private void fillMatchDetails(MatchEntity match) {
        this.match = match;
        Table matchDetailTable = new Table();

        matchDetailTable.add(new ScaledLabel("Battle against", app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .6f));

        BackendUserLabel opponentLabel = new BackendUserLabel(match, app, "default");
        opponentLabel.getLabel().setFontScale(1f);
        opponentLabel.setMaxLabelWidth(LightBlocksGame.nativeGameWidth - 50);
        matchDetailTable.row().padBottom(5);
        matchDetailTable.add(opponentLabel);

        matchDetailTable.row();
        matchDetailTable.add(new ScaledLabel(BackendScoreDetailsScreen.findI18NIfExistant(app.TEXTS, match
                .matchState, "mmturn_"), app.skin, LightBlocksGame.SKIN_FONT_TITLE, .6f)).padTop(40);

        if (match.myTurn) {
            matchDetailTable.row();
            if (match.matchState == MatchEntity.PLAYER_STATE_CHALLENGED) {
                // TODO aufgefordert: annehmen oder ablehnen
            } else {
                matchDetailTable.add(playTurnButton).padTop(20);
                //TODO aufgeben

            }
        }

        contentCell.setActor(matchDetailTable);
    }

    @Override
    protected boolean hasScrollPane() {
        return true;
    }
}
