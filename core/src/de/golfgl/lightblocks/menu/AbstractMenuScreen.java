package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.BetterScrollPane;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 24.02.2017.
 */
public abstract class AbstractMenuScreen extends AbstractScreen {
    private Button leaveButton;
    private ScrollPane menuScrollPane;

    public AbstractMenuScreen(LightBlocksGame app) {
        super(app);
    }

    protected ScrollPane getMenuScrollPane() {
        return menuScrollPane;
    }

    public Button getLeaveButton() {
        return leaveButton;
    }

    public void initializeUI() {

        Table menuTable = new Table();
        fillMenuTable(menuTable);
        // setFillParent verursacht Probleme mit ScrollPane
        menuTable.setFillParent(false);
        menuScrollPane = new BetterScrollPane(menuTable, app.skin);
        menuScrollPane.setSize(LightBlocksGame.nativeGameWidth, 150);
        menuScrollPane.setScrollingDisabled(true, false);

        //Titel
        // Der Titel wird nach der Menütabelle gefüllt, eventuell wird dort etwas gesetzt (=> Scores)
        Label title = new Label(getTitle().toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        final String subtitle = getSubtitle();

        // Buttons
        Table buttons = new Table();

        // Back button
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        setBackButton(leaveButton);
        buttons.add(leaveButton).uniform();
        fillButtonTable(buttons);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.row().padTop(15);
        mainTable.add(new Label(getTitleIcon(), app.skin, FontAwesome.SKIN_FONT_FA));
        mainTable.row();
        mainTable.add(title);
        mainTable.row();
        if (subtitle != null)
            mainTable.add(new ScaledLabel(subtitle, app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        mainTable.row().spaceTop(30);
        mainTable.add(menuScrollPane);
        mainTable.row();
        mainTable.add(buttons).spaceTop(30);

        stage.addActor(mainTable);

    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);
        app.controllerMappings.setInputProcessor(stage);

        swoshIn();

    }

    protected abstract String getTitleIcon();

    protected void fillButtonTable(Table buttons) {

    }

    protected abstract String getSubtitle();

    protected abstract String getTitle();

    protected abstract void fillMenuTable(Table menuTable);

}
