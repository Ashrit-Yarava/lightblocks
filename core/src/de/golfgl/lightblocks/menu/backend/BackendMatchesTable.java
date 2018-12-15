package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.HashMap;
import java.util.List;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

/**
 * Created by Benjamin Schulte on 18.11.2018.
 */

public class BackendMatchesTable extends WidgetGroup {
    private static final int ROW_WIDTH = 400;
    private static final int ROW_HEIGHT = 40;
    private static final int X_PADDING = 30;
    private final LightBlocksGame app;
    private long listTimeStamp;
    private HashMap<String, BackendMatchRow> uuidMatchMap = new HashMap<>(1);
    private float lastLayoutHeight;

    public BackendMatchesTable(LightBlocksGame app) {
        this.app = app;

        refresh();
    }

    public static String formatTimePassedString(LightBlocksGame app, long lastChangeTime) {
        long minutesPassed = TimeUtils.timeSinceMillis(lastChangeTime) / (1000 * 60);

        if (minutesPassed <= 1) {
            return app.TEXTS.get("timeNow");
        } else if (minutesPassed < 60)
            return app.TEXTS.format("timeXMinutes", minutesPassed);


        int hoursPassed = (int) (minutesPassed / 60);

        if (hoursPassed <= 1)
            return app.TEXTS.get("time1hour");
        else
            return app.TEXTS.format("timeXhours", hoursPassed);

    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (app.backendManager.getMultiplayerMatchesLastFetchMs() > listTimeStamp)
            refresh();
    }

    private void refresh() {
        List<MatchEntity> shownMatchesList = app.backendManager.getMultiplayerMatchesList();
        listTimeStamp = app.backendManager.getMultiplayerMatchesLastFetchMs();
        HashMap<String, BackendMatchRow> newMatchesMap = new HashMap<>(shownMatchesList.size());

        float newHeight = calcPrefHeight(shownMatchesList.size());
        if (newHeight != lastLayoutHeight)
            for (Actor a : getChildren()) {
                a.setY(a.getY() + newHeight - lastLayoutHeight);
            }
        lastLayoutHeight = newHeight;
        for (int i = 0; i < shownMatchesList.size(); i++) {
            MatchEntity me = shownMatchesList.get(i);

            float yPos = newHeight - (i + 1) * ROW_HEIGHT;

            BackendMatchRow backendMatchRow;
            if (!uuidMatchMap.containsKey(me.uuid)) {
                backendMatchRow = new BackendMatchRow(me);
                addActor(backendMatchRow);
                backendMatchRow.setPosition(X_PADDING, yPos);
                backendMatchRow.getColor().a = 0;
                backendMatchRow.addAction(Actions.delay(.15f, Actions.fadeIn(.25f, Interpolation.fade)));
            } else {
                backendMatchRow = uuidMatchMap.remove(me.uuid);
                backendMatchRow.setMatchEntity(me);
                if (yPos != backendMatchRow.getY())
                    backendMatchRow.addAction(Actions.moveTo(X_PADDING, yPos, .3f, Interpolation.fade));
            }
            newMatchesMap.put(me.uuid, backendMatchRow);
        }

        for (BackendMatchRow row : uuidMatchMap.values()) {
            row.removeFocusables();
            row.remove();
            //Die Folgende Zeile löst fehlerhafterweise auch fadeout des ersten Elements aus???
            //row.addAction(Actions.sequence(Actions.fadeOut(.2f, Interpolation.fade), Actions.removeActor()));
        }

        uuidMatchMap = newMatchesMap;

    }

    @Override
    public float getPrefHeight() {
        return calcPrefHeight(uuidMatchMap.size());
    }

    public int calcPrefHeight(int shownEntries) {
        return ROW_HEIGHT * Math.max(shownEntries, 5);
    }

    @Override
    public float getPrefWidth() {
        return LightBlocksGame.nativeGameWidth * .7f;
    }

    private class BackendMatchRow extends Button {
        private MatchEntity me;

        public BackendMatchRow(MatchEntity match) {
            super(app.skin, LightBlocksGame.SKIN_BUTTON_SMOKE);

            setMatchEntity(match);
            setSize(ROW_WIDTH, ROW_HEIGHT);
        }

        public MatchEntity getMatchEntity() {
            return me;
        }

        public void removeFocusables() {
            if (getStage() != null && getStage() instanceof ControllerMenuStage)
                ((ControllerMenuStage) getStage()).removeFocusableActor(this);
        }

        @Override
        protected void setStage(Stage stage) {
            super.setStage(stage);

            if (stage != null && stage instanceof ControllerMenuStage)
                ((ControllerMenuStage) stage).addFocusableActor(this);
        }

        public void setMatchEntity(MatchEntity match) {
            if (me == null || me.lastChangeTime < match.lastChangeTime) {
                clear();
                defaults().padRight(10);
                ScaledLabel opponentLabel = new ScaledLabel(match.opponentNick != null ? match.opponentNick : "???", app
                        .skin, LightBlocksGame.SKIN_FONT_TITLE);
                opponentLabel.setEllipsis(true);
                add(opponentLabel).width(150);
                add(new ScaledLabel(match.matchState, app.skin, LightBlocksGame.SKIN_FONT_BIG)).width(80);
                ScaledLabel timePassed = new ScaledLabel(formatTimePassedString(app, match.lastChangeTime), app.skin);
                timePassed.setEllipsis(true);
                add(timePassed).width(90);
            }
            me = match;
        }
    }
}