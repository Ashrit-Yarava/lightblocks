package de.golfgl.lightblocks.menu.multiplayer;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.multiplayer.ServerMultiplayerManager;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Shows lobby of a multiplayer server
 */
public class ServerLobbyScreen extends AbstractFullScreenDialog {
    protected final ProgressDialog.WaitRotationImage waitRotationImage;
    protected final Cell contentCell;
    private final ServerMultiplayerManager serverMultiplayerManager;
    private boolean connecting = true;

    public ServerLobbyScreen(LightBlocksGame app, String roomAddress) {
        super(app);
        waitRotationImage = new ProgressDialog.WaitRotationImage(app);
        closeButton.setFaText(FontAwesome.MISC_CROSS);

        // Fill Content
        fillFixContent();
        Table contentTable = getContentTable();
        contentTable.row();
        contentCell = contentTable.add().minHeight(waitRotationImage.getHeight() * 3);

        reload();
        serverMultiplayerManager = new ServerMultiplayerManager();
        serverMultiplayerManager.connect(roomAddress);
        connecting = true;
    }

    protected void fillFixContent() {
        Table contentTable = getContentTable();
        contentTable.row();
        contentTable.add(new Label(FontAwesome.NET_PEOPLE, app.skin, FontAwesome.SKIN_FONT_FA));
    }

    protected void reload() {
        contentCell.setActor(waitRotationImage);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (connecting && !serverMultiplayerManager.isConnecting() && serverMultiplayerManager.getLastErrorMsg() != null) {
            fillErrorScreen(serverMultiplayerManager.getLastErrorMsg());
            connecting = false;
        } else if (connecting && serverMultiplayerManager.isConnected()) {
            connecting = false;
            contentCell.setActor(null); // TODO
        }
    }

    protected Table fillErrorScreen(String errorMessage) {
        // TODO on GWT, improve error message here
        Table errorTable = new Table();
        Label errorMsgLabel = new ScaledLabel(errorMessage, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        float noWrapHeight = errorMsgLabel.getPrefHeight();
        errorMsgLabel.setWrap(true);
        errorMsgLabel.setAlignment(Align.center);
        errorTable.add(errorMsgLabel).minHeight(noWrapHeight * 1.5f).fill()
                .minWidth(LightBlocksGame.nativeGameWidth - 50);

        contentCell.setActor(errorTable).fillX();

        return errorTable;
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
        if (parent == null && serverMultiplayerManager.isConnected()) {
            serverMultiplayerManager.disconnect();
        }
    }
}
