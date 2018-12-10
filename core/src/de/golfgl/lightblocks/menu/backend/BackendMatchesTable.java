package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

import java.util.HashMap;
import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

/**
 * Created by Benjamin Schulte on 18.11.2018.
 */

public class BackendMatchesTable extends WidgetGroup {
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
                backendMatchRow.setHeight(ROW_HEIGHT);
                backendMatchRow.getColor().a = 0;
                backendMatchRow.addAction(Actions.delay(.15f, Actions.fadeIn(.25f, Interpolation.fade)));
            } else {
                backendMatchRow = uuidMatchMap.remove(me.uuid);
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

    private class BackendMatchRow extends HorizontalGroup {
        private MatchEntity me;

        public BackendMatchRow(MatchEntity match) {
            me = match;
            //TODO das muss in diesem Fall genauere Anzeige (Minuten/Stunden) sein
            addActor(new ScaledLabel(BackendScoreTable.formatTimePassedString(app, match.lastChangeTime), app.skin));

        }

        public MatchEntity getMatchEntity() {
            return me;
        }

        public void removeFocusables() {
            // TODO die entfernten Actor von der focusable-List entfernen
        }
    }
}
